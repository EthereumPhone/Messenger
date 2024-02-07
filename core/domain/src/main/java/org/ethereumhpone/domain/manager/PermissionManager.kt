package org.ethereumhpone.domain.manager

interface PermissionManager {
    fun isDefaultSms(): Boolean
    fun hasReadSms(): Boolean
    fun hasSendSms(): Boolean
    fun hasContacts(): Boolean
    fun hasPhone(): Boolean
    fun hasCalling(): Boolean
    fun hasStorage(): Boolean
}