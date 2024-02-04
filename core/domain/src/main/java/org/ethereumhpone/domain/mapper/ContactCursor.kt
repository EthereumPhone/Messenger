package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.Contact

interface ContactCursor: Mapper<Cursor, Contact> {

    fun getContactsCursor(): Cursor?

}