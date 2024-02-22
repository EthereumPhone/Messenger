package org.ethereumhpone.data.manager

import org.ethereumhpone.domain.manager.ActiveConversationManager
import javax.inject.Inject


class ActiveConversationManagerImpl @Inject constructor(): ActiveConversationManager {
    private var threadId: Long? = null

    override fun setActiveConversation(threadId: Long?) {
        this.threadId = threadId
    }

    override fun getActiveConversation(): Long? {
        return threadId
    }
}