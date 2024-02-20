package org.ethereumhpone.data.repository

import android.database.Cursor
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.domain.mapper.ConversationCursor

class ConversationCursorImpl : ConversationCursor {
    override fun getConversationsCursor(): Cursor? {
        // Your logic to retrieve a Cursor
        return TODO("Provide the return value")
    }

    override fun map(input: Cursor): Conversation {
        // Your mapping logic from Cursor to Conversation
        return TODO("Provide the return value")
    }
}