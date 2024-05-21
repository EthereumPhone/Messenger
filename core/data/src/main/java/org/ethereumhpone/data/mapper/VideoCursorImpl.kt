package org.ethereumhpone.data.mapper

import android.provider.MediaStore

class VideoCursorImpl {

    companion object {
        val URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_ADDED
        )
    }
}