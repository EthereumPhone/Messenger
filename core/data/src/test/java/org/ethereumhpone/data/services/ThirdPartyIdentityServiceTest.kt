package org.ethereumhpone.data.services

import org.junit.Assert.*
import org.junit.Test

class ThirdPartyIdentityServiceTest {

    @Test
    fun `permission constant matches manifest declaration`() {
        assertEquals(
            "org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY",
            ThirdPartyIdentityService.PERMISSION_GENERATE_IDENTITY
        )
    }

    @Test
    fun `permission constant is not empty`() {
        assertTrue(
            ThirdPartyIdentityService.PERMISSION_GENERATE_IDENTITY.isNotBlank()
        )
    }

    @Test
    fun `permission follows android naming convention`() {
        val perm = ThirdPartyIdentityService.PERMISSION_GENERATE_IDENTITY
        assertTrue(
            "Permission should use dot-separated package-style naming",
            perm.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*\\.[A-Z_]+"))
        )
    }

    @Test
    fun `both permissions are distinct`() {
        assertNotEquals(
            "The two permissions must be different",
            ThirdPartyMessagingService.PERMISSION_SEND_MESSAGE,
            ThirdPartyIdentityService.PERMISSION_GENERATE_IDENTITY
        )
    }
}
