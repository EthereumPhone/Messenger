package org.ethereumhpone.domain.model

import org.xmtp.android.library.Client
import org.xmtp.android.library.Conversation

class XMTPConversationHandler {
    private val allConversations = hashMapOf<String, Conversation>()

    suspend fun getOrCreateConversation(client: Client, address: String): Conversation {
        return allConversations.getOrPut(address) {
            client.conversations.newConversation(address)
        }
    }
}