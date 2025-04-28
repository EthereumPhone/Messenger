package org.ethereumhpone.domain.manager

interface ActiveConversationManager {

    fun setActiveConversation(threadId: String)

    fun getActiveConversation(): String?
}