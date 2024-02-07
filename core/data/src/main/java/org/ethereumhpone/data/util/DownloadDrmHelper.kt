package org.ethereumhpone.data.util

import android.content.Context
import android.drm.DrmManagerClient
import timber.log.Timber


object DownloadDrmHelper {
    /** The MIME type of special DRM files  */
    const val MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message"

    /** The extensions of special DRM files  */
    const val EXTENSION_DRM_MESSAGE = ".dm"
    const val EXTENSION_INTERNAL_FWDL = ".fl"

    /**
     * Checks if the Media Type is a DRM Media Type
     *
     * @param mimetype Media Type to check
     * @return True if the Media Type is DRM else false
     */
    fun isDrmMimeType(context: Context?, mimetype: String?): Boolean {
        var result = false
        if (context != null) {
            try {
                val drmClient = DrmManagerClient(context)
                if (drmClient != null && mimetype != null && mimetype.length > 0) {
                    result = drmClient.canHandle("", mimetype)
                }
            } catch (e: IllegalArgumentException) {
                Timber.w("DrmManagerClient instance could not be created, context is Illegal.")
            } catch (e: IllegalStateException) {
                Timber.w("DrmManagerClient didn't initialize properly.")
            }
        }
        return result
    }

    /**
     * Checks if the Media Type needs to be DRM converted
     *
     * @param mimetype Media type of the content
     * @return True if convert is needed else false
     */
    fun isDrmConvertNeeded(mimetype: String): Boolean {
        return MIMETYPE_DRM_MESSAGE == mimetype
    }

    /**
     * Modifies the file extension for a DRM Forward Lock file NOTE: This
     * function shouldn't be called if the file shouldn't be DRM converted
     */
    fun modifyDrmFwLockFileExtension(filename: String?): String? {
        var filename = filename
        if (filename != null) {
            val extensionIndex: Int
            extensionIndex = filename.lastIndexOf(".")
            if (extensionIndex != -1) {
                filename = filename.substring(0, extensionIndex)
            }
            filename = filename + EXTENSION_INTERNAL_FWDL
        }
        return filename
    }

    /**
     * Gets the original mime type of DRM protected content.
     *
     * @param context The context
     * @param path Path to the file
     * @param containingMime The current mime type of of the file i.e. the
     * containing mime type
     * @return The original mime type of the file if DRM protected else the
     * currentMime
     */
    fun getOriginalMimeType(context: Context?, path: String?, containingMime: String?): String? {
        var result = containingMime
        val drmClient = DrmManagerClient(context)
        try {
            if (drmClient.canHandle(path, null)) {
                result = drmClient.getOriginalMimeType(path)
            }
        } catch (ex: IllegalArgumentException) {
            Timber.w("Can't get original mime type since path is null or empty string.")
        } catch (ex: IllegalStateException) {
            Timber.w("DrmManagerClient didn't initialize properly.")
        }
        return result
    }
}
