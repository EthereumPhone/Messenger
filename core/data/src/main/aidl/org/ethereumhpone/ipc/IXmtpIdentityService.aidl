// IXmtpIdentityService.aidl
// AIDL interface for third-party apps to generate an isolated XMTP identity
// and send messages through it. Each calling app gets its own unique identity
// keyed by package name + signing certificate hash. No permission required.

package org.ethereumhpone.ipc;

interface IXmtpIdentityService {

    // Creates a new isolated XMTP identity for the calling app.
    // If the app already has an identity, returns its existing address.
    // Returns the Ethereum address of the identity.
    // BLOCKING: May take 10+ seconds on first call (network registration).
    String createIdentity();

    // Returns true if the calling app already has a generated identity.
    boolean hasIdentity();

    // Returns the Ethereum address of the calling app's identity, or null if none.
    String getIdentityAddress();

    // Returns the XMTP inbox ID of the calling app's identity, or null if none.
    String getInboxId();

    // Sends a direct message from the isolated identity to the given address.
    // Automatically initializes the identity's XMTP client if needed.
    // Returns the XMTP message ID on success, null on failure.
    // BLOCKING: Must be called from a background thread.
    String sendMessage(String recipientAddress, String body);
}
