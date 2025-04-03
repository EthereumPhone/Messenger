package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.ContactEntity

interface ContactCursor: Mapper<Cursor, ContactEntity> {

    fun getContactsCursor(): Cursor?

}