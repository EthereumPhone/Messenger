package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.domain.model.Attachment

interface ImageCursor: Mapper<Cursor, Attachment.Image> {

    fun getImageCursor(): Cursor?
}