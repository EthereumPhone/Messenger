package org.ethereumhpone.data.manager

import org.ethereumhpone.domain.manager.ActiveConversationManager
import javax.inject.Inject


class ActiveConversationManagerImpl @Inject constructor(): ActiveConversationManager {
    private var threadId: String? = null

    override fun setActiveConversation(threadId: String) {
        this.threadId = threadId
    }

    override fun getActiveConversation(): String? {
        return threadId
    }

    override fun clearActiveConversation() {
        this.threadId = null
    }
}