// IMessagingService.aidl
// AIDL interface for third-party apps to send messages as the logged-in user.
// Requires the SEND_MESSAGE_AS_USER permission.

package org.ethereumhpone.ipc;

interface IMessagingService {

    // Returns true if the XMTP client is initialized and ready.
    boolean isClientReady();

    // Returns the logged-in user's Ethereum address.
    String getUserAddress();

    // Returns the logged-in user's XMTP inbox ID.
    String getInboxId();

    // Sends a direct message to the given Ethereum address.
    // Creates or reuses an existing DM conversation.
    // Returns the XMTP message ID on success, null on failure.
    // BLOCKING: Must be called from a background thread.
    String sendMessage(String recipientAddress, String body);

    // Sends a message to an existing conversation (DM or group) by its ID.
    // Returns the XMTP message ID on success, null on failure.
    // BLOCKING: Must be called from a background thread.
    String sendGroupMessage(String conversationId, String body);
}
