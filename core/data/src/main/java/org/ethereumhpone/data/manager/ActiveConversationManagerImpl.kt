package org.ethereumhpone.data.manager

import org.ethereumhpone.domain.manager.ActiveConversationManager


class ActiveConversationManagerImpl: ActiveConversationManager {
    private var threadId: Long? = null

    override fun setActiveConversation(threadId: Long?) {
        this.threadId = threadId
    }

    override fun getActiveConversation(): Long? {
        return threadId
    }
}