package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.ContactGroup

interface ContactGroupCursor : Mapper<Cursor, ContactGroup> {

    fun getContactGroupsCursor(): Cursor?

}