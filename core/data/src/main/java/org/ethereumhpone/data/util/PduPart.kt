package org.ethereumhpone.data.util

import android.net.Uri


class PduPart {
    /**
     * Header of part.
     */
    private var mPartHeader: MutableMap<Int, Any>? = null
    /**
     * @return The Uri of the part data or null if the data wasn't set or
     * the data is stored as byte array.
     * @see .getData
     */
    /**
     * Set data uri. The data are stored as Uri.
     *
     * @param uri the uri
     */
    /**
     * Data uri.
     */
    var dataUri: Uri? = null

    /**
     * Part data.
     */
    private var mPartData: ByteArray? = null

    /**
     * Empty Constructor.
     */
    init {
        mPartHeader = HashMap()
    }

    var data: ByteArray?
        /**
         * @return A copy of the part data or null if the data wasn't set or
         * the data is stored as Uri.
         * @see .getDataUri
         */
        get() {
            if (mPartData == null) {
                return null
            }
            val byteArray = ByteArray(mPartData!!.size)
            System.arraycopy(mPartData, 0, byteArray, 0, mPartData!!.size)
            return byteArray
        }
        /**
         * Set part data. The data are stored as byte array.
         *
         * @param data the data
         */
        set(data) {
            if (data == null) {
                return
            }
            mPartData = ByteArray(data.size)
            System.arraycopy(data, 0, mPartData, 0, data.size)
        }
    val dataLength: Int
        /**
         * @return The length of the data, if this object have data, else 0.
         */
        get() = if (mPartData != null) {
            mPartData!!.size
        } else {
            0
        }
    var contentId: ByteArray?
        /**
         * Get Content-id value.
         *
         * @return the value
         */
        get() = mPartHeader!![P_CONTENT_ID] as ByteArray?
        /**
         * Set Content-id value
         *
         * @param contentId the content-id value
         * @throws NullPointerException if the value is null.
         */
        set(contentId) {
            require(!(contentId == null || contentId.size == 0)) { "Content-Id may not be null or empty." }
            if (contentId.size > 1 && Char(contentId[0].toUShort()) == '<' && Char(contentId[contentId.size - 1].toUShort()) == '>') {
                mPartHeader!![P_CONTENT_ID] = contentId
                return
            }

            // Insert beginning '<' and trailing '>' for Content-Id.
            val buffer = ByteArray(contentId.size + 2)
            buffer[0] = (0xff and '<'.code).toByte()
            buffer[buffer.size - 1] = (0xff and '>'.code).toByte()
            System.arraycopy(contentId, 0, buffer, 1, contentId.size)
            mPartHeader!![P_CONTENT_ID] = buffer
        }
    var charset: Int
        /**
         * Get Char-set value
         *
         * @return the charset value. Return 0 if charset was not set.
         */
        get() {
            val charset = mPartHeader!![P_CHARSET] as Int
            return charset ?: 0
        }
        /**
         * Set Char-set value.
         *
         * @param charset the value
         */
        set(charset) {
            mPartHeader!![P_CHARSET] = charset
        }
    var contentLocation: ByteArray?
        /**
         * Get Content-Location value.
         *
         * @return the value
         * return PduPart.disposition[0] instead of <Octet 128> (Form-data).
         * return PduPart.disposition[1] instead of <Octet 129> (Attachment).
         * return PduPart.disposition[2] instead of <Octet 130> (Inline).
        </Octet></Octet></Octet> */
        get() = mPartHeader!![P_CONTENT_LOCATION] as ByteArray?
        /**
         * Set Content-Location value.
         *
         * @param contentLocation the value
         * @throws NullPointerException if the value is null.
         */
        set(contentLocation) {
            if (contentLocation == null) {
                throw NullPointerException("null content-location")
            }
            mPartHeader!![P_CONTENT_LOCATION] = contentLocation
        }
    var contentDisposition: ByteArray?
        /**
         * Get Content-Disposition value.
         *
         * @return the value
         */
        get() = mPartHeader!![P_CONTENT_DISPOSITION] as ByteArray?
        /**
         * Set Content-Disposition value.
         * Use PduPart.disposition[0] instead of <Octet 128> (Form-data).
         * Use PduPart.disposition[1] instead of <Octet 129> (Attachment).
         * Use PduPart.disposition[2] instead of <Octet 130> (Inline).
         *
         * @param contentDisposition the value
         * @throws NullPointerException if the value is null.
        </Octet></Octet></Octet> */
        set(contentDisposition) {
            if (contentDisposition == null) {
                throw NullPointerException("null content-disposition")
            }
            mPartHeader!![P_CONTENT_DISPOSITION] = contentDisposition
        }
    var contentType: ByteArray?
        /**
         * Get Content-Type value of part.
         *
         * @return the value
         */
        get() = mPartHeader!![P_CONTENT_TYPE] as ByteArray?
        /**
         * Set Content-Type value.
         *
         * @param contentType the value
         * @throws NullPointerException if the value is null.
         */
        set(contentType) {
            if (contentType == null) {
                throw NullPointerException("null content-type")
            }
            mPartHeader!![P_CONTENT_TYPE] = contentType
        }
    var contentTransferEncoding: ByteArray?
        /**
         * Get Content-Transfer-Encoding value.
         *
         * @return the value
         */
        get() = mPartHeader!![P_CONTENT_TRANSFER_ENCODING] as ByteArray?
        /**
         * Set Content-Transfer-Encoding value
         *
         * @param contentTransferEncoding the content-id value
         * @throws NullPointerException if the value is null.
         */
        set(contentTransferEncoding) {
            if (contentTransferEncoding == null) {
                throw NullPointerException("null content-transfer-encoding")
            }
            mPartHeader!![P_CONTENT_TRANSFER_ENCODING] = contentTransferEncoding
        }
    var name: ByteArray?
        /**
         * Get content-type parameter: name.
         *
         * @return the name
         */
        get() = mPartHeader!![P_NAME] as ByteArray?
        /**
         * Set Content-type parameter: name.
         *
         * @param name the name value
         * @throws NullPointerException if the value is null.
         */
        set(name) {
            if (null == name) {
                throw NullPointerException("null content-id")
            }
            mPartHeader!![P_NAME] = name
        }
    var filename: ByteArray?
        /**
         * Set Content-disposition parameter: filename
         *
         * @return the filename
         */
        get() = mPartHeader!![P_FILENAME] as ByteArray?
        /**
         * Get Content-disposition parameter: filename
         *
         * @param fileName the filename value
         * @throws NullPointerException if the value is null.
         */
        set(fileName) {
            if (null == fileName) {
                throw NullPointerException("null content-id")
            }
            mPartHeader!![P_FILENAME] = fileName
        }

    fun generateLocation(): String {
        // Assumption: At least one of the content-location / name / filename
        // or content-id should be set. This is guaranteed by the PduParser
        // for incoming messages and by MM composer for outgoing messages.
        var location = mPartHeader!![P_NAME] as ByteArray?
        if (null == location) {
            location = mPartHeader!![P_FILENAME] as ByteArray?
            if (null == location) {
                location = mPartHeader!![P_CONTENT_LOCATION] as ByteArray?
            }
        }
        return if (null == location) {
            val contentId = mPartHeader!![P_CONTENT_ID] as ByteArray?
            "cid:" + String(contentId!!)
        } else {
            String(location)
        }
    }

    companion object {
        /**
         * Well-Known Parameters.
         */
        const val P_Q = 0x80
        const val P_CHARSET = 0x81
        const val P_LEVEL = 0x82
        const val P_TYPE = 0x83
        const val P_DEP_NAME = 0x85
        const val P_DEP_FILENAME = 0x86
        const val P_DIFFERENCES = 0x87
        const val P_PADDING = 0x88

        // This value of "TYPE" s used with Content-Type: multipart/related
        const val P_CT_MR_TYPE = 0x89
        const val P_DEP_START = 0x8A
        const val P_DEP_START_INFO = 0x8B
        const val P_DEP_COMMENT = 0x8C
        const val P_DEP_DOMAIN = 0x8D
        const val P_MAX_AGE = 0x8E
        const val P_DEP_PATH = 0x8F
        const val P_SECURE = 0x90
        const val P_SEC = 0x91
        const val P_MAC = 0x92
        const val P_CREATION_DATE = 0x93
        const val P_MODIFICATION_DATE = 0x94
        const val P_READ_DATE = 0x95
        const val P_SIZE = 0x96
        const val P_NAME = 0x97
        const val P_FILENAME = 0x98
        const val P_START = 0x99
        const val P_START_INFO = 0x9A
        const val P_COMMENT = 0x9B
        const val P_DOMAIN = 0x9C
        const val P_PATH = 0x9D

        /**
         * Header field names.
         */
        const val P_CONTENT_TYPE = 0x91
        const val P_CONTENT_LOCATION = 0x8E
        const val P_CONTENT_ID = 0xC0
        const val P_DEP_CONTENT_DISPOSITION = 0xAE
        const val P_CONTENT_DISPOSITION = 0xC5

        // The next header is unassigned header, use reserved header(0x48) value.
        const val P_CONTENT_TRANSFER_ENCODING = 0xC8

        /**
         * Content=Transfer-Encoding string.
         */
        const val CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding"

        /**
         * Value of Content-Transfer-Encoding.
         */
        const val P_BINARY = "binary"
        const val P_7BIT = "7bit"
        const val P_8BIT = "8bit"
        const val P_BASE64 = "base64"
        const val P_QUOTED_PRINTABLE = "quoted-printable"

        /**
         * Value of disposition can be set to PduPart when the value is octet in
         * the PDU.
         * "from-data" instead of Form-data<Octet 128>.
         * "attachment" instead of Attachment<Octet 129>.
         * "inline" instead of Inline<Octet 130>.
        </Octet></Octet></Octet> */
        val DISPOSITION_FROM_DATA = "from-data".toByteArray()
        val DISPOSITION_ATTACHMENT = "attachment".toByteArray()
        val DISPOSITION_INLINE = "inline".toByteArray()

        /**
         * Content-Disposition value.
         */
        const val P_DISPOSITION_FROM_DATA = 0x80
        const val P_DISPOSITION_ATTACHMENT = 0x81
        const val P_DISPOSITION_INLINE = 0x82
    }
}

