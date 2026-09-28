package org.ethereumhpone.data.util

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.Member
import org.xmtp.android.library.libxmtp.PublicIdentity

class MemberExtensionsTest {

    private fun identity(kind: IdentityKind, identifier: String) = mockk<PublicIdentity> {
        every { this@mockk.kind } returns kind
        every { this@mockk.identifier } returns identifier
    }

    private fun member(vararg identities: PublicIdentity) = mockk<Member> {
        every { this@mockk.identities } returns identities.toList()
    }

    @Test
    fun `returns the ethereum address`() {
        val member = member(identity(IdentityKind.ETHEREUM, "0xabc"))

        assertEquals("0xabc", member.ethereumAddressOrNull())
    }

    @Test
    fun `picks the ethereum identity when the member has several`() {
        val member = member(
            identity(IdentityKind.PASSKEY, "passkey-id"),
            identity(IdentityKind.ETHEREUM, "0xabc"),
        )

        assertEquals("0xabc", member.ethereumAddressOrNull())
    }

    // Regression: `identities.first { ETHEREUM }` threw NoSuchElementException here,
    // which cancelled the whole XMTP sync.
    @Test
    fun `returns null for a member without an ethereum identity`() {
        val member = member(identity(IdentityKind.PASSKEY, "passkey-id"))

        assertNull(member.ethereumAddressOrNull())
    }

    @Test
    fun `returns null for a member without identities`() {
        assertNull(member().ethereumAddressOrNull())
    }
}
