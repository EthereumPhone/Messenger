package org.ethereumhpone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ethereumphone.model.Conversation

interface ConversationRepository {

    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(conversationId: String): Flow<Conversation>


}