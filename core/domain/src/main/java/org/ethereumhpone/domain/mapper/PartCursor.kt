package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.MmsPart

interface PartCursor : Mapper<Cursor, MmsPart> {

    fun getPartsCursor(messageId: Long? = null): Cursor?

}