package org.ethereumhpone.domain.model

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.view.inputmethod.InputContentInfoCompat

sealed class Attachment {
    data class Image(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null,
        private val date: Long? = null
    ) : Attachment() {
        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun isGif(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && inputContent != null) {
                inputContent.description.hasMimeType("image/gif")
            } else {
                uri?.let(context.contentResolver::getType) == "image/gif"
            }
        }
    }

    data class Video(
        private val uri: Uri? = null,
        private val duration: Long? = null,
        private val date: Long? = null
    ): Attachment() {
        fun getUri(): Uri? {
            return uri
        }

        fun getThumbnail(context: Context): Bitmap? {
            return uri?.let {
                val metadata = MediaMetadataRetriever()
                metadata.setDataSource(context, it)
                metadata.primaryImage
            }
        }

        fun getDuration(): Long? {
            return duration
        }

        fun getDate(): Long? {
            return date
        }

    }

    data class Contact(
        val lookupKey: String? = null,
        val imageUri: Uri? = null,
        val vCard: String
    ) : Attachment()
}

class Attachments(attachments: List<Attachment>) : List<Attachment> by attachments
