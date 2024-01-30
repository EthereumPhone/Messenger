package org.ethereumhpone.domain.model

import org.ethereumhpone.database.model.Conversation

data class SearchResult(
    val query: String,
    val conversation: Conversation,
    val messages: Int
)
