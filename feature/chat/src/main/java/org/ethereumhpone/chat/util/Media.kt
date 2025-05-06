package org.ethereumhpone.chat.util

import android.net.Uri


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