package org.ethereumhpone.data.services

import org.junit.Assert.*
import org.junit.Test

class ThirdPartyMessagingServiceTest {

    @Test
    fun `permission constant matches manifest declaration`() {
        assertEquals(
            "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER",
            ThirdPartyMessagingService.PERMISSION_SEND_MESSAGE
        )
    }

    @Test
    fun `permission constant is not empty`() {
        assertTrue(
            ThirdPartyMessagingService.PERMISSION_SEND_MESSAGE.isNotBlank()
        )
    }

    @Test
    fun `permission follows android naming convention`() {
        val perm = ThirdPartyMessagingService.PERMISSION_SEND_MESSAGE
        assertTrue(
            "Permission should use dot-separated package-style naming",
            perm.matches(Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*\\.[A-Z_]+"))
        )
    }
}
