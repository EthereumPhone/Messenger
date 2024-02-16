package org.ethereumhpone.domain.manager

interface ActiveConversationManager {

    fun setActiveConversation(threadId: Long?)

    fun getActiveConversation(): Long?
}