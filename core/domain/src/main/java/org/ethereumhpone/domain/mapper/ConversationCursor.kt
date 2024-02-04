package org.ethereumhpone.domain.mapper

import android.database.Cursor
import org.ethereumhpone.database.model.Conversation

interface ConversationCursor : Mapper<Cursor, Conversation> {

    fun getConversationsCursor(): Cursor?

}