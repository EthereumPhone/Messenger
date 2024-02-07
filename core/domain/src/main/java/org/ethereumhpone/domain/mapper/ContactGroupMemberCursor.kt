package org.ethereumhpone.domain.mapper

import android.database.Cursor

interface ContactGroupMemberCursor : Mapper<Cursor, ContactGroupMemberCursor.GroupMember> {

    data class GroupMember(val lookupKey: String, val groupId: Long)

    fun getGroupMembersCursor(): Cursor?

}