package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.RecipientEntity

interface RecipientCursor : Mapper<Cursor, RecipientEntity> {

    fun getRecipientCursor(): Cursor?

    fun getRecipientCursor(id: Long): Cursor?

}