package org.ethereumhpone.domain.model

import org.ethereumhpone.database.model.ConversationEntity

data class SearchResult(
    val query: String,
    val conversationEntity: ConversationEntity,
    val messages: Int
)
