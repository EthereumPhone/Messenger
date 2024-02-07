package org.ethereumhpone.data.util


class PduCacheEntry(val pdu: GenericPdu, val messageBox: Int, val threadId: Long) {
    fun getPdu(): GenericPdu {
        return pdu
    }

    fun getMessageBox(): Int {
        return messageBox
    }

    fun getThreadId(): Long {
        return threadId
    }
}
