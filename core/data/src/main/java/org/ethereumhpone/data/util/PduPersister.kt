package org.ethereumhpone.data.util

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteException
import android.drm.DrmManagerClient
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Mms.Addr
import android.provider.Telephony.MmsSms
import android.provider.Telephony.MmsSms.PendingMessages
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.TextUtils
import org.ethereumhpone.data.util.pdu_alt.MultimediaMessagePdu
import org.ethereumhpone.data.util.pdu_alt.PduHeaders
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class PduPersister private constructor(private val mContext: Context) {
    private val mContentResolver: ContentResolver
    private val mDrmManagerClient: DrmManagerClient
    private val mTelephonyManager: TelephonyManager

    init {
        mContentResolver = mContext.contentResolver
        mDrmManagerClient = DrmManagerClient(mContext)
        mTelephonyManager = mContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private fun setEncodedStringValueToHeaders(
        c: Cursor, columnIndex: Int,
        headers: PduHeaders, mapColumn: Int
    ) {
        val s = c.getString(columnIndex)
        if ((s != null) && (s.length > 0)) {
            val charsetColumnIndex = (CHARSET_COLUMN_INDEX_MAP!!.get(mapColumn))!!
            val charset = c.getInt(charsetColumnIndex)
            val value = EncodedStringValue(
                charset, getBytes(s)
            )
            headers.setEncodedStringValue(value, mapColumn)
        }
    }

    private fun setTextStringToHeaders(
        c: Cursor, columnIndex: Int,
        headers: PduHeaders, mapColumn: Int
    ) {
        val s = c.getString(columnIndex)
        if (s != null) {
            headers.setTextString(getBytes(s), mapColumn)
        }
    }

    @Throws(InvalidHeaderValueException::class)
    private fun setOctetToHeaders(
        c: Cursor, columnIndex: Int,
        headers: PduHeaders, mapColumn: Int
    ) {
        if (!c.isNull(columnIndex)) {
            val b = c.getInt(columnIndex)
            headers.setOctet(b, mapColumn)
        }
    }

    private fun setLongToHeaders(
        c: Cursor, columnIndex: Int,
        headers: PduHeaders, mapColumn: Int
    ) {
        if (!c.isNull(columnIndex)) {
            val l = c.getLong(columnIndex)
            headers.setLongInteger(l, mapColumn)
        }
    }

    private fun getIntegerFromPartColumn(c: Cursor, columnIndex: Int): Int? {
        if (!c.isNull(columnIndex)) {
            return c.getInt(columnIndex)
        }
        return null
    }

    private fun getByteArrayFromPartColumn(c: Cursor, columnIndex: Int): ByteArray? {
        if (!c.isNull(columnIndex)) {
            return getBytes(c.getString(columnIndex))
        }
        return null
    }

    @Throws(MmsException::class)
    private fun loadParts(msgId: Long): Array<PduPart?>? {
        val c: Cursor? = SqliteWrapper.query(
            mContext, mContentResolver,
            Uri.parse("content://mms/$msgId/part"),
            PART_PROJECTION, null, null, null
        )
        var parts: Array<PduPart?>? = null
        try {
            if ((c == null) || (c.count == 0)) {
                if (LOCAL_LOGV) {
                    //Timber.v("loadParts($msgId): no part to load.")
                }
                return null
            }
            val partCount = c.count
            var partIdx = 0
            parts = arrayOfNulls<PduPart>(partCount)
            while (c.moveToNext()) {
                val part = PduPart()
                val charset = getIntegerFromPartColumn(
                    c, PART_COLUMN_CHARSET
                )
                if (charset != null) {
                    part.charset = charset
                }
                val contentDisposition = getByteArrayFromPartColumn(
                    c, PART_COLUMN_CONTENT_DISPOSITION
                )
                if (contentDisposition != null) {
                    part.contentDisposition = contentDisposition
                }
                val contentId = getByteArrayFromPartColumn(
                    c, PART_COLUMN_CONTENT_ID
                )
                if (contentId != null) {
                    part.contentId = contentId
                }
                val contentLocation = getByteArrayFromPartColumn(
                    c, PART_COLUMN_CONTENT_LOCATION
                )
                if (contentLocation != null) {
                    part.contentLocation = contentLocation
                }
                val contentType = getByteArrayFromPartColumn(
                    c, PART_COLUMN_CONTENT_TYPE
                )
                if (contentType != null) {
                    part.contentType = contentType
                } else {
                    throw MmsException("Content-Type must be set.")
                }
                val fileName = getByteArrayFromPartColumn(
                    c, PART_COLUMN_FILENAME
                )
                if (fileName != null) {
                    part.filename = fileName
                }
                val name = getByteArrayFromPartColumn(
                    c, PART_COLUMN_NAME
                )
                if (name != null) {
                    part.name = name
                }

                // Construct a Uri for this part.
                val partId = c.getLong(PART_COLUMN_ID)
                val partURI = Uri.parse("content://mms/part/$partId")
                part.dataUri = partURI

                // For images/audio/video, we won't keep their data in Part
                // because their renderer accept Uri as source.
                val type = toIsoString(contentType)
                if ((!ContentType.isImageType(type)
                            && !ContentType.isAudioType(type)
                            && !ContentType.isVideoType(type))
                ) {
                    val baos = ByteArrayOutputStream()
                    var `is`: InputStream? = null

                    // Store simple string values directly in the database instead of an
                    // external file.  This makes the text searchable and retrieval slightly
                    // faster.
                    if ((ContentType.TEXT_PLAIN.equals(type) || ContentType.APP_SMIL.equals(type)
                                || ContentType.TEXT_HTML.equals(type))
                    ) {
                        val text = c.getString(PART_COLUMN_TEXT)
                        val blob: ByteArray? = EncodedStringValue(if (text != null) text else "")
                            .textString
                        baos.write(blob, 0, blob!!.size)
                    } else {
                        try {
                            `is` = mContentResolver.openInputStream(partURI)
                            val buffer = ByteArray(256)
                            var len = `is`!!.read(buffer)
                            while (len >= 0) {
                                baos.write(buffer, 0, len)
                                len = `is`.read(buffer)
                            }
                        } catch (e: IOException) {
                            //Timber.e(e, "Failed to load part data")
                            c.close()
                            throw MmsException(e)
                        } finally {
                            if (`is` != null) {
                                try {
                                    `is`.close()
                                } catch (e: IOException) {
                                    //Timber.e(e, "Failed to close stream")
                                } // Ignore
                            }
                        }
                    }
                    part.data = baos.toByteArray()
                }
                parts!!.get(partIdx++) = part
            }
        } finally {
            if (c != null) {
                c.close()
            }
        }
        return parts
    }

    private fun loadAddress(msgId: Long, headers: PduHeaders) {
        val c: Cursor? = SqliteWrapper.query(
            mContext,
            mContentResolver,
            Uri.parse("content://mms/$msgId/addr"),
            arrayOf<String>(Addr.ADDRESS, Addr.CHARSET, Addr.TYPE),
            null,
            null,
            null
        )
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    val addr = c.getString(0)
                    if (!TextUtils.isEmpty(addr)) {
                        val addrType = c.getInt(2)
                        when (addrType) {
                            PduHeaders.FROM -> headers.setEncodedStringValue(
                                EncodedStringValue(c.getInt(1), getBytes(addr)),
                                addrType
                            )

                            PduHeaders.TO, PduHeaders.CC, PduHeaders.BCC -> headers.appendEncodedStringValue(
                                EncodedStringValue(c.getInt(1), getBytes(addr)),
                                addrType
                            )

                            else -> Timber.e("Unknown address type: $addrType")
                        }
                    }
                }
            } finally {
                c.close()
            }
        }
    }

    /**
     * Load a PDU from storage by given Uri.
     *
     * @param uri The Uri of the PDU to be loaded.
     * @return A generic PDU object, it may be cast to dedicated PDU.
     * @throws MmsException Failed to load some fields of a PDU.
     */
    @Throws(MmsException::class)
    fun load(uri: Uri): GenericPdu? {
        var pdu: GenericPdu? = null
        var cacheEntry: PduCacheEntry? = null
        var msgBox = 0
        var threadId = DUMMY_THREAD_ID
        try {
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) {
                        //Timber.v("load: " + uri + " blocked by isUpdating()")
                    }
                    try {
                        PDU_CACHE_INSTANCE.wait()
                    } catch (e: InterruptedException) {
                        //Timber.e(e, "load: ")
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri)
                    if (cacheEntry != null) {
                        return cacheEntry!!.pdu
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true)
            }
            val c: Cursor? = SqliteWrapper.query(
                mContext, mContentResolver, uri,
                PDU_PROJECTION, null, null, null
            )
            val headers = PduHeaders()
            var set: Set<Map.Entry<Int, Int>>
            val msgId = ContentUris.parseId(uri)
            try {
                if ((c == null) || (c.count != 1) || !c.moveToFirst()) {
                    throw MmsException("Bad uri: $uri")
                }
                msgBox = c.getInt(PDU_COLUMN_MESSAGE_BOX)
                threadId = c.getLong(PDU_COLUMN_THREAD_ID)
                set = ENCODED_STRING_COLUMN_INDEX_MAP!!.entries
                for (e: Map.Entry<Int, Int> in set) {
                    setEncodedStringValueToHeaders(
                        c, e.value, headers, e.key
                    )
                }
                set = TEXT_STRING_COLUMN_INDEX_MAP!!.entries
                for (e: Map.Entry<Int, Int> in set) {
                    setTextStringToHeaders(
                        c, e.value, headers, e.key
                    )
                }
                set = OCTET_COLUMN_INDEX_MAP!!.entries
                for (e: Map.Entry<Int, Int> in set) {
                    setOctetToHeaders(
                        c, e.value, headers, e.key
                    )
                }
                set = LONG_COLUMN_INDEX_MAP!!.entries
                for (e: Map.Entry<Int, Int> in set) {
                    setLongToHeaders(
                        c, e.value, headers, e.key
                    )
                }
            } finally {
                if (c != null) {
                    c.close()
                }
            }

            // Check whether 'msgId' has been assigned a valid value.
            if (msgId == -1L) {
                throw MmsException("Error! ID of the message: -1.")
            }

            // Load address information of the MM.
            loadAddress(msgId, headers)
            val msgType = headers.getOctet(PduHeaders.MESSAGE_TYPE)
            val body = PduBody()

            // For PDU which type is M_retrieve.conf or Send.req, we should
            // load multiparts and put them into the body of the PDU.
            if (((msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                        || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ))
            ) {
                val parts: Array<PduPart?>? = loadParts(msgId)
                if (parts != null) {
                    val partsNum = parts.size
                    for (i in 0 until partsNum) {
                        body.addPart(parts.get(i))
                    }
                }
            }
            when (msgType) {
                PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND -> pdu = NotificationInd(headers)
                PduHeaders.MESSAGE_TYPE_DELIVERY_IND -> pdu = DeliveryInd(headers)
                PduHeaders.MESSAGE_TYPE_READ_ORIG_IND -> pdu = ReadOrigInd(headers)
                PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF -> pdu = RetrieveConf(headers, body)
                PduHeaders.MESSAGE_TYPE_SEND_REQ -> pdu = SendReq(headers, body)
                PduHeaders.MESSAGE_TYPE_ACKNOWLEDGE_IND -> pdu = AcknowledgeInd(headers)
                PduHeaders.MESSAGE_TYPE_NOTIFYRESP_IND -> pdu = NotifyRespInd(headers)
                PduHeaders.MESSAGE_TYPE_READ_REC_IND -> pdu = ReadRecInd(headers)
                PduHeaders.MESSAGE_TYPE_SEND_CONF, PduHeaders.MESSAGE_TYPE_FORWARD_REQ, PduHeaders.MESSAGE_TYPE_FORWARD_CONF, PduHeaders.MESSAGE_TYPE_MBOX_STORE_REQ, PduHeaders.MESSAGE_TYPE_MBOX_STORE_CONF, PduHeaders.MESSAGE_TYPE_MBOX_VIEW_REQ, PduHeaders.MESSAGE_TYPE_MBOX_VIEW_CONF, PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_REQ, PduHeaders.MESSAGE_TYPE_MBOX_UPLOAD_CONF, PduHeaders.MESSAGE_TYPE_MBOX_DELETE_REQ, PduHeaders.MESSAGE_TYPE_MBOX_DELETE_CONF, PduHeaders.MESSAGE_TYPE_MBOX_DESCR, PduHeaders.MESSAGE_TYPE_DELETE_REQ, PduHeaders.MESSAGE_TYPE_DELETE_CONF, PduHeaders.MESSAGE_TYPE_CANCEL_REQ, PduHeaders.MESSAGE_TYPE_CANCEL_CONF -> throw MmsException(
                    "Unsupported PDU type: " + Integer.toHexString(msgType)
                )

                else -> throw MmsException(
                    "Unrecognized PDU type: " + Integer.toHexString(msgType)
                )
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                if (pdu != null) {
                    assert((PDU_CACHE_INSTANCE.get(uri) == null))
                    // Update the cache entry with the real info
                    cacheEntry = PduCacheEntry(pdu, msgBox, threadId)
                    PDU_CACHE_INSTANCE.put(uri, cacheEntry)
                }
                PDU_CACHE_INSTANCE.setUpdating(uri, false)
                PDU_CACHE_INSTANCE.notifyAll() // tell anybody waiting on this entry to go ahead
            }
        }
        return pdu
    }

    private fun persistAddress(msgId: Long, type: Int, array: Array<EncodedStringValue?>) {
        val values = ContentValues(3)
        for (addr: EncodedStringValue? in array) {
            values.clear() // Clear all values first.
            values.put(Addr.ADDRESS, toIsoString(addr!!.textString))
            values.put(Addr.CHARSET, addr.characterSet)
            values.put(Addr.TYPE, type)
            val uri = Uri.parse("content://mms/$msgId/addr")
            SqliteWrapper.insert(mContext, mContentResolver, uri, values)
        }
    }

    @Throws(MmsException::class)
    fun persistPart(part: PduPart, msgId: Long, preOpenedFiles: HashMap<Uri?, InputStream>?): Uri {
        val uri = Uri.parse("content://mms/$msgId/part")
        val values = ContentValues(8)
        val charset: Int = part.charset
        if (charset != 0) {
            values.put(Mms.Part.CHARSET, charset)
        }
        var contentType = getPartContentType(part)
        if (contentType != null) {
            // There is no "image/jpg" in Android (and it's an invalid mimetype).
            // Change it to "image/jpeg"
            if (ContentType.IMAGE_JPG.equals(contentType)) {
                contentType = ContentType.IMAGE_JPEG
            }
            values.put(Mms.Part.CONTENT_TYPE, contentType)
            // To ensure the SMIL part is always the first part.
            if (ContentType.APP_SMIL.equals(contentType)) {
                values.put(Mms.Part.SEQ, -1)
            }
        } else {
            throw MmsException("MIME type of the part must be set.")
        }
        if (part.filename != null) {
            val fileName: String = part.filename.toString()
            values.put(Mms.Part.FILENAME, fileName)
        }
        if (part.name != null) {
            val name: String = part.name.toString()
            values.put(Mms.Part.NAME, name)
        }
        var value: Any? = null
        if (part.contentDisposition != null) {
            value = toIsoString(part.contentDisposition)
            values.put(Mms.Part.CONTENT_DISPOSITION, value as String?)
        }
        if (part.contentId != null) {
            value = toIsoString(part.contentId)
            values.put(Mms.Part.CONTENT_ID, value as String?)
        }
        if (part.contentLocation != null) {
            value = toIsoString(part.contentLocation)
            values.put(Mms.Part.CONTENT_LOCATION, value as String?)
        }
        val res: Uri = SqliteWrapper.insert(mContext, mContentResolver, uri, values)
            ?: throw MmsException("Failed to persist part, return null.")
        persistData(part, res, contentType, preOpenedFiles)
        // After successfully store the data, we should update
        // the dataUri of the part.
        part.dataUri = res
        return res
    }

    /**
     * Save data of the part into storage. The source data may be given
     * by a byte[] or a Uri. If it's a byte[], directly save it
     * into storage, otherwise load source data from the dataUri and then
     * save it. If the data is an image, we may scale down it according
     * to user preference.
     *
     * @param part           The PDU part which contains data to be saved.
     * @param uri            The URI of the part.
     * @param contentType    The MIME type of the part.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Cannot find source data or error occurred
     * while saving the data.
     */
    @Throws(MmsException::class)
    private fun persistData(
        part: PduPart, uri: Uri?,
        contentType: String?, preOpenedFiles: HashMap<Uri?, InputStream>?
    ) {
        var os: OutputStream? = null
        var `is`: InputStream? = null
        var drmConvertSession: DrmConvertSession? = null
        var dataUri: Uri? = null
        var path: String? = null
        try {
            var data: ByteArray? = part.data
            if ((ContentType.TEXT_PLAIN.equals(contentType)
                        || ContentType.APP_SMIL.equals(contentType)
                        || ContentType.TEXT_HTML.equals(contentType))
            ) {
                val cv = ContentValues()
                if (data == null) {
                    data = "".toByteArray(Charset.forName(CharacterSets.DEFAULT_CHARSET_NAME))
                }
                val dataText: String = EncodedStringValue(data).string
                cv.put(Mms.Part.TEXT, dataText)
                if (mContentResolver.update((uri)!!, cv, null, null) != 1) {
                    if (data!!.size > MAX_TEXT_BODY_SIZE) {
                        val cv2 = ContentValues()
                        cv2.put(Mms.Part.TEXT, cutString(dataText, MAX_TEXT_BODY_SIZE))
                        if (mContentResolver.update((uri), cv2, null, null) != 1) {
                            throw MmsException("unable to update $uri")
                        }
                    } else {
                        throw MmsException("unable to update $uri")
                    }
                }
            } else {
                val isDrm: Boolean = DownloadDrmHelper.isDrmConvertNeeded(contentType!!)
                if (isDrm) {
                    if (uri != null) {
                        try {
                            path = convertUriToPath(mContext, uri)
                            if (LOCAL_LOGV) {
                                //Timber.v("drm uri: $uri path: $path")
                            }
                            val f = File(path)
                            val len = f.length()
                            if (LOCAL_LOGV) {
                                //Timber.v("drm path: $path len: $len")
                            }
                            if (len > 0) {
                                // we're not going to re-persist and re-encrypt an already
                                // converted drm file
                                return
                            }
                        } catch (e: Exception) {
                            //Timber.e(e, "Can't get file info for: " + part.getDataUri())
                        }
                    }
                    // We haven't converted the file yet, start the conversion
                    drmConvertSession = DrmConvertSession.open(mContext, contentType)
                    if (drmConvertSession == null) {
                        throw MmsException(
                            ("Mimetype " + contentType +
                                    " can not be converted.")
                        )
                    }
                }
                // uri can look like:
                // content://mms/part/98
                os = mContentResolver.openOutputStream((uri)!!)
                if (data == null) {
                    dataUri = part.dataUri
                    if ((dataUri == null) || (dataUri === uri)) {
                        //Timber.w("Can't find data for this part.")
                        return
                    }
                    // dataUri can look like:
                    // content://com.google.android.gallery3d.provider/picasa/item/5720646660183715586
                    if (preOpenedFiles != null && preOpenedFiles.containsKey(dataUri)) {
                        `is` = preOpenedFiles.get(dataUri)
                    }
                    if (`is` == null) {
                        `is` = mContentResolver.openInputStream(dataUri)
                    }
                    if (LOCAL_LOGV) {
                        Timber.v("Saving data to: $uri")
                    }
                    val buffer = ByteArray(8192)
                    var len = 0
                    while ((`is`!!.read(buffer).also { len = it }) != -1) {
                        if (!isDrm) {
                            os!!.write(buffer, 0, len)
                        } else {
                            val convertedData: ByteArray = drmConvertSession.convert(buffer, len)
                            if (convertedData != null) {
                                os!!.write(convertedData, 0, convertedData.size)
                            } else {
                                throw MmsException("Error converting drm data.")
                            }
                        }
                    }
                } else {
                    if (LOCAL_LOGV) {
                        Timber.v("Saving data to: $uri")
                    }
                    if (!isDrm) {
                        os!!.write(data)
                    } else {
                        dataUri = uri
                        val convertedData: ByteArray = drmConvertSession.convert(data, data.size)
                        if (convertedData != null) {
                            os!!.write(convertedData, 0, convertedData.size)
                        } else {
                            throw MmsException("Error converting drm data.")
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            Timber.e(e, "Failed to open Input/Output stream.")
            throw MmsException(e)
        } catch (e: IOException) {
            Timber.e(e, "Failed to read/write data.")
            throw MmsException(e)
        } finally {
            if (os != null) {
                try {
                    os.close()
                } catch (e: IOException) {
                    Timber.e(e, "IOException while closing: $os")
                } // Ignore
            }
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    Timber.e(e, "IOException while closing: $`is`")
                } // Ignore
            }
            if (drmConvertSession != null) {
                drmConvertSession.close(path)

                // Reset the permissions on the encrypted part file so everyone has only read
                // permission.
                val f = File(path)
                val values = ContentValues(0)
                SqliteWrapper.update(
                    mContext, mContentResolver,
                    Uri.parse("content://mms/resetFilePerm/" + f.name),
                    values, null, null
                )
            }
        }
    }

    private fun updateAddress(
        msgId: Long, type: Int, array: Array<EncodedStringValue?>
    ) {
        // Delete old address information and then insert new ones.
        SqliteWrapper.delete(
            mContext, mContentResolver,
            Uri.parse("content://mms/$msgId/addr"),
            Addr.TYPE + "=" + type, null
        )
        persistAddress(msgId, type, array)
    }

    /**
     * Update headers of a SendReq.
     *
     * @param uri     The PDU which need to be updated.
     * @param sendReq New headers.
     * @throws MmsException Bad URI or updating failed.
     */
    fun updateHeaders(uri: Uri, sendReq: SendReq) {
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) {
                    Timber.v("updateHeaders: " + uri + " blocked by isUpdating()")
                }
                try {
                    PDU_CACHE_INSTANCE.wait()
                } catch (e: InterruptedException) {
                    Timber.e(e, "updateHeaders: ")
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri)
        val values = ContentValues(10)
        val contentType: ByteArray = sendReq.getContentType()
        if (contentType != null) {
            values.put(Mms.CONTENT_TYPE, toIsoString(contentType))
        }
        val date: Long = sendReq.getDate()
        if (date != -1L) {
            values.put(Mms.DATE, date)
        }
        val deliveryReport: Int = sendReq.getDeliveryReport()
        if (deliveryReport != 0) {
            values.put(Mms.DELIVERY_REPORT, deliveryReport)
        }
        val expiry: Long = sendReq.getExpiry()
        if (expiry != -1L) {
            values.put(Mms.EXPIRY, expiry)
        }
        val msgClass: ByteArray = sendReq.getMessageClass()
        if (msgClass != null) {
            values.put(Mms.MESSAGE_CLASS, toIsoString(msgClass))
        }
        val priority: Int = sendReq.getPriority()
        if (priority != 0) {
            values.put(Mms.PRIORITY, priority)
        }
        val readReport: Int = sendReq.getReadReport()
        if (readReport != 0) {
            values.put(Mms.READ_REPORT, readReport)
        }
        val transId: ByteArray = sendReq.getTransactionId()
        if (transId != null) {
            values.put(Mms.TRANSACTION_ID, toIsoString(transId))
        }
        val subject: EncodedStringValue = sendReq.getSubject()
        if (subject != null) {
            values.put(Mms.SUBJECT, toIsoString(subject.textString))
            values.put(Mms.SUBJECT_CHARSET, subject.characterSet)
        } else {
            values.put(Mms.SUBJECT, "")
        }
        val messageSize: Long = sendReq.getMessageSize()
        if (messageSize > 0) {
            values.put(Mms.MESSAGE_SIZE, messageSize)
        }
        val headers: PduHeaders = sendReq.getPduHeaders()
        val recipients = HashSet<String>()
        for (addrType: Int in ADDRESS_FIELDS) {
            var array: Array<EncodedStringValue?>? = null
            if (addrType == PduHeaders.FROM) {
                val v = headers.getEncodedStringValue(addrType)
                if (v != null) {
                    array = arrayOfNulls(1)
                    array.get(0) = v
                }
            } else {
                array = headers.getEncodedStringValues(addrType)
            }
            if (array != null) {
                val msgId = ContentUris.parseId(uri)
                updateAddress(msgId, addrType, array)
                if (addrType == PduHeaders.TO) {
                    for (v: EncodedStringValue? in array) {
                        if (v != null) {
                            recipients.add(v.string)
                        }
                    }
                }
            }
        }
        if (!recipients.isEmpty()) {
            val threadId = Telephony.Threads.getOrCreateThreadId(mContext, recipients)
            values.put(Mms.THREAD_ID, threadId)
        }
        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null)
    }

    @Throws(MmsException::class)
    private fun updatePart(uri: Uri, part: PduPart, preOpenedFiles: HashMap<Uri?, InputStream>?) {
        val values = ContentValues(7)
        val charset: Int = part.charset
        if (charset != 0) {
            values.put(Mms.Part.CHARSET, charset)
        }
        var contentType: String? = null
        if (part.getContentType() != null) {
            contentType = toIsoString(part.getContentType())
            values.put(Mms.Part.CONTENT_TYPE, contentType)
        } else {
            throw MmsException("MIME type of the part must be set.")
        }
        if (part.getFilename() != null) {
            val fileName: String = String(part.getFilename())
            values.put(Mms.Part.FILENAME, fileName)
        }
        if (part.getName() != null) {
            val name: String = String(part.getName())
            values.put(Mms.Part.NAME, name)
        }
        var value: Any? = null
        if (part.getContentDisposition() != null) {
            value = toIsoString(part.getContentDisposition())
            values.put(Mms.Part.CONTENT_DISPOSITION, value as String?)
        }
        if (part.getContentId() != null) {
            value = toIsoString(part.getContentId())
            values.put(Mms.Part.CONTENT_ID, value as String?)
        }
        if (part.getContentLocation() != null) {
            value = toIsoString(part.getContentLocation())
            values.put(Mms.Part.CONTENT_LOCATION, value as String?)
        }
        SqliteWrapper.update(mContext, mContentResolver, uri, values, null, null)

        // Only update the data when:
        // 1. New binary data supplied or
        // 2. The Uri of the part is different from the current one.
        if (((part.getData() != null)
                    || (uri !== part.getDataUri()))
        ) {
            persistData(part, uri, contentType, preOpenedFiles)
        }
    }

    /**
     * Update all parts of a PDU.
     *
     * @param uri            The PDU which need to be updated.
     * @param body           New message body of the PDU.
     * @param preOpenedFiles if not null, a map of preopened InputStreams for the parts.
     * @throws MmsException Bad URI or updating failed.
     */
    @Throws(MmsException::class)
    fun updateParts(uri: Uri, body: PduBody, preOpenedFiles: HashMap<Uri?, InputStream>?) {
        try {
            var cacheEntry: PduCacheEntry
            synchronized(PDU_CACHE_INSTANCE) {
                if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                    if (LOCAL_LOGV) {
                        Timber.v("updateParts: " + uri + " blocked by isUpdating()")
                    }
                    try {
                        PDU_CACHE_INSTANCE.wait()
                    } catch (e: InterruptedException) {
                        Timber.e(e, "updateParts: ")
                    }
                    cacheEntry = PDU_CACHE_INSTANCE.get(uri)
                    if (cacheEntry != null) {
                        (cacheEntry.getPdu() as MultimediaMessagePdu).setBody(body)
                    }
                }
                // Tell the cache to indicate to other callers that this item
                // is currently being updated.
                PDU_CACHE_INSTANCE.setUpdating(uri, true)
            }
            val toBeCreated: ArrayList<PduPart> = ArrayList<PduPart>()
            val toBeUpdated: HashMap<Uri, PduPart> = HashMap<Uri, PduPart>()
            val partsNum: Int = body.getPartsNum()
            val filter = StringBuilder().append('(')
            for (i in 0 until partsNum) {
                val part: PduPart = body.getPart(i)
                val partUri: Uri = part.getDataUri()
                if ((partUri == null) || !partUri.authority!!.startsWith("mms")) {
                    toBeCreated.add(part)
                } else {
                    toBeUpdated[partUri] = part

                    // Don't use 'i > 0' to determine whether we should append
                    // 'AND' since 'i = 0' may be skipped in another branch.
                    if (filter.length > 1) {
                        filter.append(" AND ")
                    }
                    filter.append(Mms.Part._ID)
                    filter.append("!=")
                    DatabaseUtils.appendEscapedSQLString(filter, partUri.lastPathSegment)
                }
            }
            filter.append(')')
            val msgId = ContentUris.parseId(uri)

            // Remove the parts which doesn't exist anymore.
            SqliteWrapper.delete(
                mContext, mContentResolver,
                Uri.parse(Mms.CONTENT_URI.toString() + "/" + msgId + "/part"),
                if (filter.length > 2) filter.toString() else null, null
            )

            // Create new parts which didn't exist before.
            for (part: PduPart in toBeCreated) {
                persistPart(part, msgId, preOpenedFiles)
            }

            // Update the modified parts.
            for (e: Map.Entry<Uri, PduPart> in toBeUpdated.entries) {
                updatePart(e.key, e.value, preOpenedFiles)
            }
        } finally {
            synchronized(PDU_CACHE_INSTANCE) {
                PDU_CACHE_INSTANCE.setUpdating(uri, false)
                PDU_CACHE_INSTANCE.notifyAll()
            }
        }
    }

    /**
     * Persist a PDU object to specific location in the storage.
     *
     * @param pdu             The PDU object to be stored.
     * @param uri             Where to store the given PDU object.
     * @param threadId
     * @param createThreadId  if true, this function may create a thread id for the recipients
     * @param groupMmsEnabled if true, all of the recipients addressed in the PDU will be used
     * to create the associated thread. When false, only the sender will be used in finding or
     * creating the appropriate thread or conversation.
     * @param preOpenedFiles  if not null, a map of preopened InputStreams for the parts.
     * @return A Uri which can be used to access the stored PDU.
     */
    @Throws(MmsException::class)
    fun persist(
        pdu: GenericPdu,
        uri: Uri?,
        threadId: Long,
        createThreadId: Boolean,
        groupMmsEnabled: Boolean,
        preOpenedFiles: HashMap<Uri?, InputStream>?
    ): Uri? {
        var threadId = threadId
        if (uri == null) {
            throw MmsException("Uri may not be null.")
        }
        var msgId: Long = -1
        try {
            msgId = ContentUris.parseId(uri)
        } catch (e: NumberFormatException) {
            // the uri ends with "inbox" or something else like that
        }
        val existingUri = msgId != -1L
        if (!existingUri && MESSAGE_BOX_MAP!!.get(uri) == null) {
            throw MmsException(
                ("Bad destination, must be one of "
                        + "content://mms/inbox, content://mms/sent, "
                        + "content://mms/drafts, content://mms/outbox, "
                        + "content://mms/temp.")
            )
        }
        synchronized(PDU_CACHE_INSTANCE) {
            // If the cache item is getting updated, wait until it's done updating before
            // purging it.
            if (PDU_CACHE_INSTANCE.isUpdating(uri)) {
                if (LOCAL_LOGV) {
                    Timber.v("persist: " + uri + " blocked by isUpdating()")
                }
                try {
                    PDU_CACHE_INSTANCE.wait()
                } catch (e: InterruptedException) {
                    Timber.e(e, "persist1: ")
                }
            }
        }
        PDU_CACHE_INSTANCE.purge(uri)
        val header: PduHeaders = pdu.getPduHeaders()
        var body: PduBody? = null
        var values = ContentValues()
        var set: Set<Map.Entry<Int, String?>>
        set = ENCODED_STRING_COLUMN_NAME_MAP!!.entries
        for (e: Map.Entry<Int, String?> in set) {
            val field = e.key
            val encodedString = header.getEncodedStringValue(field)
            if (encodedString != null) {
                val charsetColumn = CHARSET_COLUMN_NAME_MAP!!.get(field)
                values.put(e.value, toIsoString(encodedString.getTextString()))
                values.put(charsetColumn, encodedString.getCharacterSet())
            }
        }
        set = TEXT_STRING_COLUMN_NAME_MAP!!.entries
        for (e: Map.Entry<Int, String?> in set) {
            val text = header.getTextString(e.key)
            if (text != null) {
                values.put(e.value, toIsoString(text))
            }
        }
        set = OCTET_COLUMN_NAME_MAP!!.entries
        for (e: Map.Entry<Int, String?> in set) {
            val b = header.getOctet(e.key)
            if (b != 0) {
                values.put(e.value, b)
            }
        }
        set = LONG_COLUMN_NAME_MAP!!.entries
        for (e: Map.Entry<Int, String?> in set) {
            val l = header.getLongInteger(e.key)
            if (l != -1L) {
                values.put(e.value, l)
            }
        }
        val addressMap = HashMap<Int, Array<EncodedStringValue?>?>(ADDRESS_FIELDS.size)
        // Save address information.
        for (addrType: Int in ADDRESS_FIELDS) {
            var array: Array<EncodedStringValue?>? = null
            if (addrType == PduHeaders.FROM) {
                val v = header.getEncodedStringValue(addrType)
                if (v != null) {
                    array = arrayOfNulls(1)
                    array.get(0) = v
                }
            } else {
                array = header.getEncodedStringValues(addrType)
            }
            addressMap[addrType] = array
        }
        val recipients = HashSet<String>()
        val msgType: Int = pdu.getMessageType()
        // Here we only allocate thread ID for M-Notification.ind,
        // M-Retrieve.conf and M-Send.req.
        // Some of other PDU types may be allocated a thread ID outside
        // this scope.
        if (((msgType == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND)
                    || (msgType == PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)
                    || (msgType == PduHeaders.MESSAGE_TYPE_SEND_REQ))
        ) {
            when (msgType) {
                PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND, PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF -> {
                    loadRecipients(PduHeaders.FROM, recipients, addressMap, false)

                    // For received messages when group MMS is enabled, we want to associate this
                    // message with the thread composed of all the recipients -- all but our own
                    // number, that is. This includes the person who sent the
                    // message or the FROM field (above) in addition to the other people the message
                    // was addressed to or the TO field. Our own number is in that TO field and
                    // we have to ignore it in loadRecipients.
                    if (groupMmsEnabled) {
                        loadRecipients(PduHeaders.TO, recipients, addressMap, true)

                        // Also load any numbers in the CC field to address group messaging
                        // compatibility issues with devices that place numbers in this field
                        // for group messages.
                        loadRecipients(PduHeaders.CC, recipients, addressMap, true)
                    }
                }

                PduHeaders.MESSAGE_TYPE_SEND_REQ -> loadRecipients(
                    PduHeaders.TO,
                    recipients,
                    addressMap,
                    false
                )
            }
            if ((threadId == DUMMY_THREAD_ID) && createThreadId && !recipients.isEmpty()) {
                // Given all the recipients associated with this message, find (or create) the
                // correct thread.
                threadId = Telephony.Threads.getOrCreateThreadId(mContext, recipients)
            }
            values.put(Mms.THREAD_ID, threadId)
        }

        // Save parts first to avoid inconsistent message is loaded
        // while saving the parts.
        val dummyId = System.currentTimeMillis() // Dummy ID of the msg.

        // Sum up the total message size
        var messageSize = 0

        // Get body if the PDU is a RetrieveConf or SendReq.
        if (pdu is MultimediaMessagePdu) {
            body = (pdu as MultimediaMessagePdu).getBody()
            // Start saving parts if necessary.
            if (body != null) {
                for (i in 0 until body.getPartsNum()) {
                    val part: PduPart = body.getPart(i)
                    messageSize += part.getDataLength()
                    persistPart(part, dummyId, preOpenedFiles)
                }
            }
        }

        // The message-size might already have been inserted when parsing the
        // PDU header. If not, then we insert the message size as well.
        if (values.getAsInteger(Mms.MESSAGE_SIZE) == null) {
            values.put(Mms.MESSAGE_SIZE, messageSize)
        }
        var res: Uri? = null
        if (existingUri) {
            res = uri
            SqliteWrapper.update(mContext, mContentResolver, res, values, null, null)
        } else {
            res = SqliteWrapper.insert(mContext, mContentResolver, uri, values)
            if (res == null) {
                throw MmsException("persist() failed: return null.")
            }
            // Get the real ID of the PDU and update all parts which were
            // saved with the dummy ID.
            msgId = ContentUris.parseId(res)
        }
        values = ContentValues(1)
        values.put(Mms.Part.MSG_ID, msgId)
        SqliteWrapper.update(
            mContext, mContentResolver,
            Uri.parse("content://mms/$dummyId/part"),
            values, null, null
        )
        // We should return the longest URI of the persisted PDU, for
        // example, if input URI is "content://mms/inbox" and the _ID of
        // persisted PDU is '8', we should return "content://mms/inbox/8"
        // instead of "content://mms/8".
        // FIXME: Should the MmsProvider be responsible for this???
        if (!existingUri) {
            res = Uri.parse("$uri/$msgId")
        }

        // Save address information.
        for (addrType: Int in ADDRESS_FIELDS) {
            val array = addressMap.get(addrType)
            if (array != null) {
                persistAddress(msgId, addrType, array)
            }
        }
        return res
    }

    /**
     * For a given address type, extract the recipients from the headers.
     *
     * @param addressType     can be PduHeaders.FROM, PduHeaders.TO or PduHeaders.CC
     * @param recipients      a HashSet that is loaded with the recipients from the FROM, TO or CC headers
     * @param addressMap      a HashMap of the addresses from the ADDRESS_FIELDS header
     * @param excludeMyNumber if true, the number of this phone will be excluded from recipients
     */
    private fun loadRecipients(
        addressType: Int, recipients: HashSet<String>,
        addressMap: HashMap<Int, Array<EncodedStringValue?>?>, excludeMyNumber: Boolean
    ) {
        val array = addressMap.get(addressType) ?: return
        // If the TO recipients is only a single address, then we can skip loadRecipients when
        // we're excluding our own number because we know that address is our own.
        // NOTE: this is not true for project fi users. To fix it, we'll add the final check for the
        //       TO type. project fi will use the cc field instead.
        if (excludeMyNumber && (array.size == 1) && (addressType == PduHeaders.TO)) {
            return
        }
        val myNumber = if (excludeMyNumber) mTelephonyManager.line1Number else null
        for (v: EncodedStringValue? in array) {
            if (v != null) {
                val number: String = v.getString()
                if ((myNumber == null || !PhoneNumberUtils.compare(number, myNumber)) &&
                    !recipients.contains(number)
                ) {
                    // Only add numbers which aren't my own number.
                    recipients.add(number)
                }
            }
        }
    }

    /**
     * Move a PDU object from one location to another.
     *
     * @param from Specify the PDU object to be moved.
     * @param to   The destination location, should be one of the following:
     * "content://mms/inbox", "content://mms/sent",
     * "content://mms/drafts", "content://mms/outbox",
     * "content://mms/trash".
     * @return New Uri of the moved PDU.
     * @throws MmsException Error occurred while moving the message.
     */
    @Throws(MmsException::class)
    fun move(from: Uri?, to: Uri): Uri {
        // Check whether the 'msgId' has been assigned a valid value.
        val msgId = ContentUris.parseId((from)!!)
        if (msgId == -1L) {
            throw MmsException("Error! ID of the message: -1.")
        }

        // Get corresponding int value of destination box.
        val msgBox = MESSAGE_BOX_MAP!!.get(to)
            ?: throw MmsException(
                ("Bad destination, must be one of "
                        + "content://mms/inbox, content://mms/sent, "
                        + "content://mms/drafts, content://mms/outbox, "
                        + "content://mms/temp.")
            )
        val values = ContentValues(1)
        values.put(Mms.MESSAGE_BOX, msgBox)
        SqliteWrapper.update(mContext, mContentResolver, from, values, null, null)
        return ContentUris.withAppendedId(to, msgId)
    }

    /**
     * Remove all objects in the temporary path.
     */
    fun release() {
        val uri = Uri.parse(TEMPORARY_DRM_OBJECT_URI)
        SqliteWrapper.delete(mContext, mContentResolver, uri, null, null)
    }

    /**
     * Find all messages to be sent or downloaded before certain time.
     */
    fun getPendingMessages(dueTime: Long): Cursor? {
        if (!checkReadSmsPermissions()) {
            Timber.w("No read sms permissions have been granted")
            return null
        }
        val uriBuilder = PendingMessages.CONTENT_URI.buildUpon()
        uriBuilder.appendQueryParameter("protocol", "mms")
        val selection = (PendingMessages.ERROR_TYPE + " < ?"
                + " AND " + PendingMessages.DUE_TIME + " <= ?")
        val selectionArgs =
            arrayOf(MmsSms.ERR_TYPE_GENERIC_PERMANENT.toString(), dueTime.toString())
        return SqliteWrapper.query(
            mContext, mContentResolver,
            uriBuilder.build(), null, selection, selectionArgs,
            PendingMessages.DUE_TIME
        )
    }

    /**
     * Check if read permissions for SMS have been granted
     */
    private fun checkReadSmsPermissions(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                mContext.checkSelfPermission(Manifest.permission.READ_SMS) ==
                PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val LOCAL_LOGV = false
        val DUMMY_THREAD_ID = Long.MAX_VALUE
        private val DEFAULT_SUBSCRIPTION = 0
        private val MAX_TEXT_BODY_SIZE = 300 * 1024

        /**
         * The uri of temporary drm objects.
         */
        val TEMPORARY_DRM_OBJECT_URI = "content://mms/" + Long.MAX_VALUE + "/part"

        /**
         * Indicate that we transiently failed to process a MM.
         */
        val PROC_STATUS_TRANSIENT_FAILURE = 1

        /**
         * Indicate that we permanently failed to process a MM.
         */
        val PROC_STATUS_PERMANENTLY_FAILURE = 2

        /**
         * Indicate that we have successfully processed a MM.
         */
        val PROC_STATUS_COMPLETED = 3
        private var sPersister: PduPersister? = null
        private val PDU_CACHE_INSTANCE: PduCache? = null
        private val ADDRESS_FIELDS = intArrayOf(
            PduHeaders.BCC,
            PduHeaders.CC,
            PduHeaders.FROM,
            PduHeaders.TO
        )
        private val PDU_PROJECTION = arrayOf(
            Mms._ID,
            Mms.MESSAGE_BOX,
            Mms.THREAD_ID,
            Mms.RETRIEVE_TEXT,
            Mms.SUBJECT,
            Mms.CONTENT_LOCATION,
            Mms.CONTENT_TYPE,
            Mms.MESSAGE_CLASS,
            Mms.MESSAGE_ID,
            Mms.RESPONSE_TEXT,
            Mms.TRANSACTION_ID,
            Mms.CONTENT_CLASS,
            Mms.DELIVERY_REPORT,
            Mms.MESSAGE_TYPE,
            Mms.MMS_VERSION,
            Mms.PRIORITY,
            Mms.READ_REPORT,
            Mms.READ_STATUS,
            Mms.REPORT_ALLOWED,
            Mms.RETRIEVE_STATUS,
            Mms.STATUS,
            Mms.DATE,
            Mms.DELIVERY_TIME,
            Mms.EXPIRY,
            Mms.MESSAGE_SIZE,
            Mms.SUBJECT_CHARSET,
            Mms.RETRIEVE_TEXT_CHARSET
        )
        private val PDU_COLUMN_ID = 0
        private val PDU_COLUMN_MESSAGE_BOX = 1
        private val PDU_COLUMN_THREAD_ID = 2
        private val PDU_COLUMN_RETRIEVE_TEXT = 3
        private val PDU_COLUMN_SUBJECT = 4
        private val PDU_COLUMN_CONTENT_LOCATION = 5
        private val PDU_COLUMN_CONTENT_TYPE = 6
        private val PDU_COLUMN_MESSAGE_CLASS = 7
        private val PDU_COLUMN_MESSAGE_ID = 8
        private val PDU_COLUMN_RESPONSE_TEXT = 9
        private val PDU_COLUMN_TRANSACTION_ID = 10
        private val PDU_COLUMN_CONTENT_CLASS = 11
        private val PDU_COLUMN_DELIVERY_REPORT = 12
        private val PDU_COLUMN_MESSAGE_TYPE = 13
        private val PDU_COLUMN_MMS_VERSION = 14
        private val PDU_COLUMN_PRIORITY = 15
        private val PDU_COLUMN_READ_REPORT = 16
        private val PDU_COLUMN_READ_STATUS = 17
        private val PDU_COLUMN_REPORT_ALLOWED = 18
        private val PDU_COLUMN_RETRIEVE_STATUS = 19
        private val PDU_COLUMN_STATUS = 20
        private val PDU_COLUMN_DATE = 21
        private val PDU_COLUMN_DELIVERY_TIME = 22
        private val PDU_COLUMN_EXPIRY = 23
        private val PDU_COLUMN_MESSAGE_SIZE = 24
        private val PDU_COLUMN_SUBJECT_CHARSET = 25
        private val PDU_COLUMN_RETRIEVE_TEXT_CHARSET = 26
        private val PART_PROJECTION = arrayOf(
            Mms.Part._ID,
            Mms.Part.CHARSET,
            Mms.Part.CONTENT_DISPOSITION,
            Mms.Part.CONTENT_ID,
            Mms.Part.CONTENT_LOCATION,
            Mms.Part.CONTENT_TYPE,
            Mms.Part.FILENAME,
            Mms.Part.NAME,
            Mms.Part.TEXT
        )
        private val PART_COLUMN_ID = 0
        private val PART_COLUMN_CHARSET = 1
        private val PART_COLUMN_CONTENT_DISPOSITION = 2
        private val PART_COLUMN_CONTENT_ID = 3
        private val PART_COLUMN_CONTENT_LOCATION = 4
        private val PART_COLUMN_CONTENT_TYPE = 5
        private val PART_COLUMN_FILENAME = 6
        private val PART_COLUMN_NAME = 7
        private val PART_COLUMN_TEXT = 8
        private val MESSAGE_BOX_MAP: HashMap<Uri, Int?>? = null

        // These map are used for convenience in persist() and load().
        private val CHARSET_COLUMN_INDEX_MAP: HashMap<Int, Int>? = null
        private val ENCODED_STRING_COLUMN_INDEX_MAP: HashMap<Int, Int>? = null
        private val TEXT_STRING_COLUMN_INDEX_MAP: HashMap<Int, Int>? = null
        private val OCTET_COLUMN_INDEX_MAP: HashMap<Int, Int>? = null
        private val LONG_COLUMN_INDEX_MAP: HashMap<Int, Int>? = null
        private val CHARSET_COLUMN_NAME_MAP: HashMap<Int, String>? = null
        private val ENCODED_STRING_COLUMN_NAME_MAP: HashMap<Int, String?>? = null
        private val TEXT_STRING_COLUMN_NAME_MAP: HashMap<Int, String?>? = null
        private val OCTET_COLUMN_NAME_MAP: HashMap<Int, String?>? = null
        private val LONG_COLUMN_NAME_MAP: HashMap<Int, String?>? = null

        init {
            MESSAGE_BOX_MAP = HashMap()
            MESSAGE_BOX_MAP[Mms.Inbox.CONTENT_URI] = Mms.MESSAGE_BOX_INBOX
            MESSAGE_BOX_MAP[Mms.Sent.CONTENT_URI] = Mms.MESSAGE_BOX_SENT
            MESSAGE_BOX_MAP[Mms.Draft.CONTENT_URI] = Mms.MESSAGE_BOX_DRAFTS
            MESSAGE_BOX_MAP[Mms.Outbox.CONTENT_URI] = Mms.MESSAGE_BOX_OUTBOX
            CHARSET_COLUMN_INDEX_MAP = HashMap()
            CHARSET_COLUMN_INDEX_MAP[PduHeaders.SUBJECT] = PDU_COLUMN_SUBJECT_CHARSET
            CHARSET_COLUMN_INDEX_MAP[PduHeaders.RETRIEVE_TEXT] = PDU_COLUMN_RETRIEVE_TEXT_CHARSET
            CHARSET_COLUMN_NAME_MAP = HashMap()
            CHARSET_COLUMN_NAME_MAP[PduHeaders.SUBJECT] = Mms.SUBJECT_CHARSET
            CHARSET_COLUMN_NAME_MAP[PduHeaders.RETRIEVE_TEXT] = Mms.RETRIEVE_TEXT_CHARSET

            // Encoded string field code -> column index/name map.
            ENCODED_STRING_COLUMN_INDEX_MAP = HashMap()
            ENCODED_STRING_COLUMN_INDEX_MAP[PduHeaders.RETRIEVE_TEXT] =
                PDU_COLUMN_RETRIEVE_TEXT
            ENCODED_STRING_COLUMN_INDEX_MAP[PduHeaders.SUBJECT] =
                PDU_COLUMN_SUBJECT
            ENCODED_STRING_COLUMN_NAME_MAP = HashMap()
            ENCODED_STRING_COLUMN_NAME_MAP[PduHeaders.RETRIEVE_TEXT] = Mms.RETRIEVE_TEXT
            ENCODED_STRING_COLUMN_NAME_MAP[PduHeaders.SUBJECT] = Mms.SUBJECT

            // Text string field code -> column index/name map.
            TEXT_STRING_COLUMN_INDEX_MAP = HashMap()
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.CONTENT_LOCATION] =
                PDU_COLUMN_CONTENT_LOCATION
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.CONTENT_TYPE] =
                PDU_COLUMN_CONTENT_TYPE
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.MESSAGE_CLASS] =
                PDU_COLUMN_MESSAGE_CLASS
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.MESSAGE_ID] =
                PDU_COLUMN_MESSAGE_ID
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.RESPONSE_TEXT] =
                PDU_COLUMN_RESPONSE_TEXT
            TEXT_STRING_COLUMN_INDEX_MAP[PduHeaders.TRANSACTION_ID] =
                PDU_COLUMN_TRANSACTION_ID
            TEXT_STRING_COLUMN_NAME_MAP = HashMap()
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.CONTENT_LOCATION] = Mms.CONTENT_LOCATION
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.CONTENT_TYPE] = Mms.CONTENT_TYPE
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.MESSAGE_CLASS] = Mms.MESSAGE_CLASS
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.MESSAGE_ID] = Mms.MESSAGE_ID
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.RESPONSE_TEXT] = Mms.RESPONSE_TEXT
            TEXT_STRING_COLUMN_NAME_MAP[PduHeaders.TRANSACTION_ID] = Mms.TRANSACTION_ID

            // Octet field code -> column index/name map.
            OCTET_COLUMN_INDEX_MAP = HashMap()
            OCTET_COLUMN_INDEX_MAP[PduHeaders.CONTENT_CLASS] = PDU_COLUMN_CONTENT_CLASS
            OCTET_COLUMN_INDEX_MAP[PduHeaders.DELIVERY_REPORT] = PDU_COLUMN_DELIVERY_REPORT
            OCTET_COLUMN_INDEX_MAP[PduHeaders.MESSAGE_TYPE] = PDU_COLUMN_MESSAGE_TYPE
            OCTET_COLUMN_INDEX_MAP[PduHeaders.MMS_VERSION] = PDU_COLUMN_MMS_VERSION
            OCTET_COLUMN_INDEX_MAP[PduHeaders.PRIORITY] = PDU_COLUMN_PRIORITY
            OCTET_COLUMN_INDEX_MAP[PduHeaders.READ_REPORT] = PDU_COLUMN_READ_REPORT
            OCTET_COLUMN_INDEX_MAP[PduHeaders.READ_STATUS] = PDU_COLUMN_READ_STATUS
            OCTET_COLUMN_INDEX_MAP[PduHeaders.REPORT_ALLOWED] = PDU_COLUMN_REPORT_ALLOWED
            OCTET_COLUMN_INDEX_MAP[PduHeaders.RETRIEVE_STATUS] = PDU_COLUMN_RETRIEVE_STATUS
            OCTET_COLUMN_INDEX_MAP[PduHeaders.STATUS] = PDU_COLUMN_STATUS
            OCTET_COLUMN_NAME_MAP = HashMap()
            OCTET_COLUMN_NAME_MAP[PduHeaders.CONTENT_CLASS] = Mms.CONTENT_CLASS
            OCTET_COLUMN_NAME_MAP[PduHeaders.DELIVERY_REPORT] = Mms.DELIVERY_REPORT
            OCTET_COLUMN_NAME_MAP[PduHeaders.MESSAGE_TYPE] = Mms.MESSAGE_TYPE
            OCTET_COLUMN_NAME_MAP[PduHeaders.MMS_VERSION] = Mms.MMS_VERSION
            OCTET_COLUMN_NAME_MAP[PduHeaders.PRIORITY] = Mms.PRIORITY
            OCTET_COLUMN_NAME_MAP[PduHeaders.READ_REPORT] = Mms.READ_REPORT
            OCTET_COLUMN_NAME_MAP[PduHeaders.READ_STATUS] = Mms.READ_STATUS
            OCTET_COLUMN_NAME_MAP[PduHeaders.REPORT_ALLOWED] = Mms.REPORT_ALLOWED
            OCTET_COLUMN_NAME_MAP[PduHeaders.RETRIEVE_STATUS] = Mms.RETRIEVE_STATUS
            OCTET_COLUMN_NAME_MAP[PduHeaders.STATUS] = Mms.STATUS

            // Long field code -> column index/name map.
            LONG_COLUMN_INDEX_MAP = HashMap()
            LONG_COLUMN_INDEX_MAP[PduHeaders.DATE] = PDU_COLUMN_DATE
            LONG_COLUMN_INDEX_MAP[PduHeaders.DELIVERY_TIME] = PDU_COLUMN_DELIVERY_TIME
            LONG_COLUMN_INDEX_MAP[PduHeaders.EXPIRY] = PDU_COLUMN_EXPIRY
            LONG_COLUMN_INDEX_MAP[PduHeaders.MESSAGE_SIZE] = PDU_COLUMN_MESSAGE_SIZE
            LONG_COLUMN_NAME_MAP = HashMap()
            LONG_COLUMN_NAME_MAP[PduHeaders.DATE] = Mms.DATE
            LONG_COLUMN_NAME_MAP[PduHeaders.DELIVERY_TIME] = Mms.DELIVERY_TIME
            LONG_COLUMN_NAME_MAP[PduHeaders.EXPIRY] = Mms.EXPIRY
            LONG_COLUMN_NAME_MAP[PduHeaders.MESSAGE_SIZE] = Mms.MESSAGE_SIZE
            PDU_CACHE_INSTANCE = PduCache.getInstance()
        }

        /**
         * Get(or create if not exist) an instance of PduPersister
         */
        fun getPduPersister(context: Context): PduPersister? {
            if ((sPersister == null)) {
                sPersister = PduPersister(context)
            } else if (!(context == sPersister!!.mContext)) {
                sPersister!!.release()
                sPersister = PduPersister(context)
            }
            return sPersister
        }

        private fun getPartContentType(part: PduPart): String? {
            return if (part.getContentType() == null) null else toIsoString(part.getContentType())
        }

        private fun cutString(src: String, expectSize: Int): String {
            if (src.length == 0) {
                return ""
            }
            val builder = StringBuilder(expectSize)
            val length = src.length
            var i = 0
            var size = 0
            while (i < length) {
                val codePoint = Character.codePointAt(src, i)
                if (Character.charCount(codePoint) == 1) {
                    size += 1
                    if (size > expectSize) {
                        break
                    }
                    builder.append(codePoint.toChar())
                } else {
                    val chars = Character.toChars(codePoint)
                    size += chars.size
                    if (size > expectSize) {
                        break
                    }
                    builder.append(chars)
                }
                i = Character.offsetByCodePoints(src, i, 1)
            }
            return builder.toString()
        }

        /**
         * This method expects uri in the following format
         * content://media/<table_name>/<row_index> (or)
         * file://sdcard/test.mp4
         * http://test.com/test.mp4
         *
         *
         * Here <table_name> shall be "video" or "audio" or "images"
         * <row_index> the index of the content in given table
        </row_index></table_name></row_index></table_name> */
        fun convertUriToPath(context: Context, uri: Uri?): String? {
            var path: String? = null
            if (null != uri) {
                val scheme = uri.scheme
                if (((null == scheme) || (scheme == "") || (scheme == ContentResolver.SCHEME_FILE))) {
                    path = uri.path
                } else if ((scheme == "http")) {
                    path = uri.toString()
                } else if ((scheme == ContentResolver.SCHEME_CONTENT)) {
                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    var cursor: Cursor? = null
                    try {
                        cursor = context.contentResolver.query(
                            uri, projection, null,
                            null, null
                        )
                        if ((null == cursor) || (0 == cursor.count) || !cursor.moveToFirst()) {
                            throw IllegalArgumentException(
                                "Given Uri could not be found" +
                                        " in media store"
                            )
                        }
                        val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        path = cursor.getString(pathIndex)
                    } catch (e: SQLiteException) {
                        throw IllegalArgumentException(
                            "Given Uri is not formatted in a way " +
                                    "so that it can be found in media store."
                        )
                    } finally {
                        if (null != cursor) {
                            cursor.close()
                        }
                    }
                } else {
                    throw IllegalArgumentException("Given Uri scheme is not supported")
                }
            }
            return path
        }

        /**
         * Wrap a byte[] into a String.
         */
        fun toIsoString(bytes: ByteArray?): String {
            try {
                return String((bytes)!!, Charset.forName(CharacterSets.MIMENAME_ISO_8859_1))
            } catch (e: UnsupportedEncodingException) {
                // Impossible to reach here!
                e.printStackTrace()
                return ""
            }
        }

        /**
         * Unpack a given String into a byte[].
         */
        fun getBytes(data: String): ByteArray {
            try {
                return data.toByteArray(Charset.forName(CharacterSets.MIMENAME_ISO_8859_1))
            } catch (e: UnsupportedEncodingException) {
                // Impossible to reach here!
                //Timber.e(e, "ISO_8859_1 must be supported!")
                e.printStackTrace()
                return ByteArray(0)
            }
        }
    }
}
