package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.ContactEntity

interface ContactCursor: Mapper<Cursor, ContactEntity> {

    fun getContactsCursor(): Cursor?

    /**
     * Returns contacts that have an ETH address in DATA15 but may not have a phone number.
     * This supplements getContactsCursor() which only returns contacts with phone numbers.
     */
    fun getContactsWithEthAddress(): List<ContactEntity>

}