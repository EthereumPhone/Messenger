// IMsgSyncService.aidl
// AIDL interface for OS-level message sync binding
// The OS XMTPNotificationsService binds to this service and calls syncNow() periodically

package org.ethereumhpone.ipc;

// This interface is called by the OS-level XMTPNotificationsService
// to trigger synchronization of XMTP messages.
oneway interface IMsgSyncService {
    // Triggers an immediate sync of all XMTP conversations and messages.
    // This method is called every 5 minutes by the OS service.
    // It must:
    // 1. Initialize XMTP client if not ready
    // 2. Call syncAllConversations() to fetch from network
    // 3. Process all new messages and store them locally
    // 4. Trigger notifications for new unread messages
    //
    // The 'oneway' modifier makes this fire-and-forget (non-blocking for the caller).
    void syncNow();
}

