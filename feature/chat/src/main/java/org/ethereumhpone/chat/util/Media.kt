package org.ethereumhpone.chat.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import org.ethereumhpone.domain.model.Attachment


// Data class to represent media items
data class GalleryMedia(
    val id: Long,
    val uri: Uri,
    val name: String,
    val type: MediaType,
    val dateAdded: Long
)

enum class MediaType {
    IMAGE, VIDEO
}

/**
 * Turn a List<Uri> into a List<Attachment>, filling in
 * duration (for videos) and MIME (for images) plus DATE_ADDED.
 */
fun urisToAttachments(context: Context, uris: List<Uri>): List<Attachment> {
    val cr = context.contentResolver
    return uris.map { uri ->
        if (isVideoUri(context, uri)) {
            // --- video: get duration + dateAdded
            val retriever = MediaMetadataRetriever().apply { setDataSource(context, uri) }
            val durMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
            val dateAdded = queryDateAdded(cr, uri)
            Attachment.Video(uri, durMs, dateAdded)
        } else {
            // --- image: MIME type + dateAdded
            val mime = cr.getType(uri)
                ?: run {
                    // fallback based on extension
                    val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())?.lowercase()
                    ext?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
                }
            val dateAdded = queryDateAdded(cr, uri)
            Attachment.Image(uri, /* for InputContent you can pass null */ null, dateAdded)
        }
    }
}

/** Returns true if ContentResolver thinks this URI is “video/…” */
private fun isVideoUri(context: Context, uri: Uri): Boolean {
    val mime = context.contentResolver.getType(uri)
        ?: MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.lowercase()) }
        ?: ""
    return mime.startsWith("video/")
}

/** Helper to pull DATE_ADDED from any MediaStore table */
private fun queryDateAdded(cr: ContentResolver, uri: Uri): Long? {
    cr.query(uri, arrayOf(MediaStore.MediaColumns.DATE_ADDED), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
    return null
}
