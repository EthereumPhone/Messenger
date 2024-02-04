package org.ethereumhpone.domain.mapper

import android.database.Cursor

interface ContactGroupMember : Mapper<Cursor, ContactGroupMember.GroupMember> {

    data class GroupMember(val lookupKey: String, val groupId: Long)

    fun getGroupMembersCursor(): Cursor?

}