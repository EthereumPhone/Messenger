package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.ConversationEntity

interface ConversationCursor : Mapper<Cursor, ConversationEntity> {

    fun getConversationsCursor(): Cursor?

}