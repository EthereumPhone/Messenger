package org.ethereumhpone.data.util

import android.annotation.SuppressLint
import android.content.Context
import android.drm.DrmConvertedStatus
import android.drm.DrmManagerClient
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile


class DrmConvertSession private constructor(drmClient: DrmManagerClient, convertSessionId: Int) {
    private val mDrmClient: DrmManagerClient?
    private val mConvertSessionId: Int

    init {
        mDrmClient = drmClient
        mConvertSessionId = convertSessionId
    }

    /**
     * Convert a buffer of data to protected format.
     *
     * @param inBuffer Buffer filled with data to convert.
     * @param size The number of bytes that shall be converted.
     * @return A Buffer filled with converted data, if execution is ok, in all
     * other case null.
     */
    @SuppressLint("TimberArgCount")
    fun convert(inBuffer: ByteArray?, size: Int): ByteArray? {
        var result: ByteArray? = null
        if (inBuffer != null) {
            var convertedStatus: DrmConvertedStatus? = null
            try {
                if (size != inBuffer.size) {
                    val buf = ByteArray(size)
                    System.arraycopy(inBuffer, 0, buf, 0, size)
                    convertedStatus = mDrmClient!!.convertData(mConvertSessionId, buf)
                } else {
                    convertedStatus = mDrmClient!!.convertData(mConvertSessionId, inBuffer)
                }
                if (convertedStatus != null && convertedStatus.statusCode == DrmConvertedStatus.STATUS_OK && convertedStatus.convertedData != null) {
                    result = convertedStatus.convertedData
                }
            } catch (e: IllegalArgumentException) {

                Timber.w(
                    "Buffer with data to convert is illegal. Convertsession: "
                            + mConvertSessionId, e
                )
            } catch (e: IllegalStateException) {
                Timber.w(
                    "Could not convert data. Convertsession: " +
                            mConvertSessionId, e
                )
            }
        } else {
            throw IllegalArgumentException("Parameter inBuffer is null")
        }
        return result
    }

    /**
     * Ends a conversion session of a file.
     *
     * @param filename The filename of the converted file.
     * @return STATUS_SUCCESS if execution is ok.
     * STATUS_FILE_ERROR in case converted file can not
     * be accessed. STATUS_NOT_ACCEPTABLE if a problem
     * occurs when accessing drm framework.
     * STATUS_UNKNOWN_ERROR if a general error occurred.
     */
    @SuppressLint("TimberArgCount")
    fun close(filename: String): Int {
        var convertedStatus: DrmConvertedStatus? = null
        var result = STATUS_UNKNOWN_ERROR
        if (mDrmClient != null && mConvertSessionId >= 0) {
            try {
                convertedStatus = mDrmClient.closeConvertSession(mConvertSessionId)
                if ((convertedStatus == null) || (
                            convertedStatus.statusCode != DrmConvertedStatus.STATUS_OK) || (
                            convertedStatus.convertedData == null)
                ) {
                    result = STATUS_NOT_ACCEPTABLE
                } else {
                    var rndAccessFile: RandomAccessFile? = null
                    try {
                        rndAccessFile = RandomAccessFile(filename, "rw")
                        rndAccessFile.seek(convertedStatus.offset.toLong())
                        rndAccessFile.write(convertedStatus.convertedData)
                        result = STATUS_SUCCESS
                    } catch (e: FileNotFoundException) {
                        result = STATUS_FILE_ERROR
                        Timber.w(e, "File: $filename could not be found.")
                    } catch (e: IOException) {
                        result = STATUS_FILE_ERROR
                        Timber.w(e, "Could not access File: $filename .")
                    } catch (e: IllegalArgumentException) {
                        result = STATUS_FILE_ERROR
                        Timber.w(e, "Could not open file in mode: rw")
                    } catch (e: SecurityException) {
                        Timber.w(
                            ("Access to File: " + filename +
                                    " was denied denied by SecurityManager."), e
                        )
                    } finally {
                        if (rndAccessFile != null) {
                            try {
                                rndAccessFile.close()
                            } catch (e: IOException) {
                                result = STATUS_FILE_ERROR
                                /*
                                Timber.w(
                                    ("Failed to close File:" + filename
                                            + "."), e
                                )
                                 */
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: IllegalStateException) {
                /*
                Timber.w(
                    "Could not close convertsession. Convertsession: " +
                            mConvertSessionId, e
                )
                 */
                e.printStackTrace()
            }
        }
        return result
    }

    companion object {
        private val STATUS_UNKNOWN_ERROR = 491
        private val STATUS_NOT_ACCEPTABLE = 406
        private val STATUS_SUCCESS = 200
        private val STATUS_FILE_ERROR = 492

        /**
         * Start of converting a file.
         *
         * @param context The context of the application running the convert session.
         * @param mimeType Mimetype of content that shall be converted.
         * @return A convert session or null in case an error occurs.
         */
        fun open(context: Context?, mimeType: String?): DrmConvertSession? {
            var drmClient: DrmManagerClient? = null
            var convertSessionId = -1
            if ((context != null) && (mimeType != null) && mimeType != "") {
                try {
                    drmClient = DrmManagerClient(context)
                    try {
                        convertSessionId = drmClient.openConvertSession(mimeType)
                    } catch (e: IllegalArgumentException) {
                        /*
                        Timber.w(
                            ("Conversion of Mimetype: " + mimeType
                                    + " is not supported."), e
                        )
                         */
                        e.printStackTrace()

                    } catch (e: IllegalStateException) {
                        //Timber.w(e, "Could not access Open DrmFramework.")
                        e.printStackTrace()
                    }
                } catch (e: IllegalArgumentException) {
                    //Timber.w("DrmManagerClient instance could not be created, context is Illegal.")
                    e.printStackTrace()
                } catch (e: IllegalStateException) {
                    //Timber.w("DrmManagerClient didn't initialize properly.")
                    e.printStackTrace()
                }
            }
            return if (drmClient == null || convertSessionId < 0) {
                null
            } else {
                DrmConvertSession(drmClient, convertSessionId)
            }
        }
    }
}
