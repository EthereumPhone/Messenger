package org.ethereumhpone.data.mapper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.mapper.ImageCursor
import javax.inject.Inject

class ImageCursorImpl @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager
): ImageCursor {

    companion object {
        val URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
        )
    }

    override fun getImageCursor(): Cursor? {
        return when (permissionManager.hasWriteStorage()) {
            true -> context.contentResolver.query(URI, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )
            false -> null
        }
    }

    override fun map(from: Cursor): Uri {
        val id = from.getLong(from.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
    }
}