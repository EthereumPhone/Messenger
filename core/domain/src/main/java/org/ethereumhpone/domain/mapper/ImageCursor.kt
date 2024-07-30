package org.ethereumhpone.domain.mapper

import android.database.Cursor
import android.net.Uri

interface ImageCursor: Mapper<Cursor, Uri> {

    fun getImageCursor(): Cursor?
}