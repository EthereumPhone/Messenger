package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.Recipient

interface RecipientCursor : Mapper<Cursor, Recipient> {

    fun getRecipientCursor(): Cursor?

    fun getRecipientCursor(id: Long): Cursor?

}