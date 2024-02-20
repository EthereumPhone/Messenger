package org.ethereumhpone.data.repository

import android.database.Cursor
import org.ethereumhpone.database.model.Conversation
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.domain.mapper.ConversationCursor
import org.ethereumhpone.domain.mapper.RecipientCursor

class RecipientCursorImpl : RecipientCursor {
    override fun getRecipientCursor(): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getRecipientCursor(id: Long): Cursor? {
        TODO("Not yet implemented")
    }

    override fun map(from: Cursor): Recipient {
        TODO("Not yet implemented")
    }

}