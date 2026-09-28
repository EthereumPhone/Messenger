package org.ethereumhpone.data.util

import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.Member

/**
 * The member's Ethereum address, or null if the inbox has no Ethereum identity
 * (e.g. passkey-only inboxes). Callers should skip such members rather than crash.
 */
fun Member.ethereumAddressOrNull(): String? =
    identities.firstOrNull { it.kind == IdentityKind.ETHEREUM }?.identifier
