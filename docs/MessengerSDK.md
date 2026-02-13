# Third-Party XMTP Messaging Integration Guide

This document explains how third-party Android apps can integrate with the XMTP Messenger app to send messages. Two integration levels are available:

---

## Overview

| Integration | Permission required | Identity used |
|---|---|---|
| Send as user | `SEND_MESSAGE_AS_USER` (dangerous) | The user's own XMTP identity |
| Isolated identity | None | A new, app-specific identity |

Both expose bound AIDL services that your app connects to via IPC. The "Send as user" path requires a runtime permission; the isolated-identity path is permission-free — any app can bind and create its own identity.

---

## Architecture

```
┌─────────────────────┐              ┌────────────────────────────────┐
│  Your App            │              │  XMTP Messenger                │
│                      │              │                                │
│ 1. bindService()     │── IPC ────▶ │ 1. Bound AIDL Service          │
│ 2. sendMessage()     │── IPC ────▶ │ 2. Sends via XMTP protocol     │
└─────────────────────┘              └────────────────────────────────┘

For "Send as user" only, a runtime permission dialog is shown before binding.
```

---

## Permission 1: Send Messages as the User

Use this when your app needs to send messages **on behalf of the currently logged-in Messenger user**. The recipient sees the message as coming from the user's real XMTP identity.

### Step 1: Declare the permission

In your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER" />
```

### Step 2: Copy the AIDL interface

Create the file `src/main/aidl/org/ethereumhpone/ipc/IMessagingService.aidl` in your project:

```java
package org.ethereumhpone.ipc;

interface IMessagingService {
    boolean isClientReady();
    String getUserAddress();
    String getInboxId();
    String sendMessage(String recipientAddress, String body);
    String sendGroupMessage(String conversationId, String body);
}
```

> The package and method signatures must match exactly. Do **not** rename the package.

### Step 3: Request the permission at runtime

```kotlin
import androidx.core.app.ActivityCompat
import android.Manifest

private const val SEND_MESSAGE_PERMISSION =
    "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER"
private const val REQUEST_CODE = 1001

// Check and request
if (checkSelfPermission(SEND_MESSAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(SEND_MESSAGE_PERMISSION),
        REQUEST_CODE
    )
}
```

Handle the result:

```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
        // Permission granted — you can now bind to the service
        bindMessagingService()
    }
}
```

### Step 4: Bind to the service

```kotlin
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.ethereumhpone.ipc.IMessagingService

private var messagingService: IMessagingService? = null

private val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        messagingService = IMessagingService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        messagingService = null
    }
}

fun bindMessagingService() {
    val intent = Intent("org.ethereumhpone.messenger.action.BIND_MESSAGING").apply {
        setPackage("org.ethereumhpone.messenger")
    }
    bindService(intent, connection, Context.BIND_AUTO_CREATE)
}
```

### Step 5: Send messages

All IPC calls are **blocking** and must be called from a background thread.

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Send a direct message to an Ethereum address
suspend fun sendDirectMessage(address: String, body: String): String? {
    return withContext(Dispatchers.IO) {
        messagingService?.sendMessage(address, body)
    }
}

// Send to an existing conversation (DM or group) by ID
suspend fun sendToConversation(conversationId: String, body: String): String? {
    return withContext(Dispatchers.IO) {
        messagingService?.sendGroupMessage(conversationId, body)
    }
}

// Check readiness
suspend fun isReady(): Boolean {
    return withContext(Dispatchers.IO) {
        messagingService?.isClientReady() ?: false
    }
}

// Get the user's address
suspend fun getUserAddress(): String? {
    return withContext(Dispatchers.IO) {
        messagingService?.userAddress
    }
}
```

### Step 6: Unbind when done

```kotlin
override fun onDestroy() {
    super.onDestroy()
    unbindService(connection)
}
```

---

## Isolated XMTP Identity (no permission required)

Use this when your app needs **its own XMTP identity** that is separate from the user. Messages are sent from a new Ethereum address that is unique to your app. The user's own identity is never used. No runtime permission is required — any app can bind to this service.

This is ideal for:
- Bots or automated agents that need their own presence on XMTP
- Apps that want to send/receive messages without impersonating the user
- Multi-tenant scenarios where each app has its own messaging identity

### Step 1: Copy the AIDL interface

Create `src/main/aidl/org/ethereumhpone/ipc/IXmtpIdentityService.aidl`:

```java
package org.ethereumhpone.ipc;

interface IXmtpIdentityService {
    String createIdentity();
    boolean hasIdentity();
    String getIdentityAddress();
    String getInboxId();
    String sendMessage(String recipientAddress, String body);
}
```

### Step 2: Bind to the identity service

```kotlin
import org.ethereumhpone.ipc.IXmtpIdentityService

private var identityService: IXmtpIdentityService? = null

private val identityConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        identityService = IXmtpIdentityService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        identityService = null
    }
}

fun bindIdentityService() {
    val intent = Intent("org.ethereumhpone.messenger.action.BIND_IDENTITY").apply {
        setPackage("org.ethereumhpone.messenger")
    }
    bindService(intent, identityConnection, Context.BIND_AUTO_CREATE)
}
```

### Step 3: Create an identity and send messages

```kotlin
// Create the identity (only needed once — subsequent calls return the existing one)
suspend fun initializeIdentity(): String? {
    return withContext(Dispatchers.IO) {
        identityService?.createIdentity()
    }
}

// Check if identity already exists
suspend fun hasIdentity(): Boolean {
    return withContext(Dispatchers.IO) {
        identityService?.hasIdentity() ?: false
    }
}

// Get the isolated identity's address
suspend fun getMyAddress(): String? {
    return withContext(Dispatchers.IO) {
        identityService?.identityAddress
    }
}

// Send a message from the isolated identity
suspend fun sendMessage(recipientAddress: String, body: String): String? {
    return withContext(Dispatchers.IO) {
        identityService?.sendMessage(recipientAddress, body)
    }
}
```

### Step 4: Unbind when done

```kotlin
override fun onDestroy() {
    super.onDestroy()
    unbindService(identityConnection)
}
```

---

## Complete Example: Activity with Both Services

```kotlin
class MessagingActivity : AppCompatActivity() {

    private var messagingService: IMessagingService? = null
    private var identityService: IXmtpIdentityService? = null

    private val messagingConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            messagingService = IMessagingService.Stub.asInterface(binder)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            messagingService = null
        }
    }

    private val identityConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            identityService = IXmtpIdentityService.Stub.asInterface(binder)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            identityService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The identity service can be bound immediately (no permission needed).
        // The messaging service requires SEND_MESSAGE_AS_USER permission.
        if (checkSelfPermission(PERM_SEND) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(PERM_SEND), 100)
        } else {
            bindMessagingService()
        }
        bindIdentityService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            bindMessagingService()
        }
    }

    private fun bindMessagingService() {
        val intent = Intent("org.ethereumhpone.messenger.action.BIND_MESSAGING")
            .setPackage("org.ethereumhpone.messenger")
        bindService(intent, messagingConnection, Context.BIND_AUTO_CREATE)
    }

    private fun bindIdentityService() {
        val intent = Intent("org.ethereumhpone.messenger.action.BIND_IDENTITY")
            .setPackage("org.ethereumhpone.messenger")
        bindService(intent, identityConnection, Context.BIND_AUTO_CREATE)
    }

    // Example: send as the logged-in user (requires permission)
    fun sendAsUser(address: String, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val messageId = messagingService?.sendMessage(address, message)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MessagingActivity,
                    if (messageId != null) "Sent: $messageId" else "Failed to send",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Example: send from isolated identity (no permission needed)
    fun sendFromIsolatedIdentity(address: String, message: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Ensure identity exists
            val myAddress = identityService?.createIdentity()

            // Send
            val messageId = identityService?.sendMessage(address, message)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MessagingActivity,
                    "Sent from $myAddress: $messageId",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(messagingConnection)
        unbindService(identityConnection)
    }

    companion object {
        private const val PERM_SEND =
            "org.ethereumhpone.messenger.permission.SEND_MESSAGE_AS_USER"
    }
}
```

---

## AIDL Method Reference

### IMessagingService

| Method | Returns | Description |
|---|---|---|
| `isClientReady()` | `boolean` | Whether the XMTP client is initialized |
| `getUserAddress()` | `String?` | The logged-in user's Ethereum address |
| `getInboxId()` | `String?` | The logged-in user's XMTP inbox ID |
| `sendMessage(address, body)` | `String?` | Send DM to an Ethereum address. Returns message ID |
| `sendGroupMessage(convId, body)` | `String?` | Send to existing conversation by ID. Returns message ID |

### IXmtpIdentityService

| Method | Returns | Description |
|---|---|---|
| `createIdentity()` | `String?` | Generate (or retrieve) isolated identity. Returns address |
| `hasIdentity()` | `boolean` | Whether this app already has an identity |
| `getIdentityAddress()` | `String?` | The isolated identity's Ethereum address |
| `getInboxId()` | `String?` | The isolated identity's XMTP inbox ID |
| `sendMessage(address, body)` | `String?` | Send DM from isolated identity. Returns message ID |

---

## Important Notes

### Threading
All AIDL methods that involve network operations (`sendMessage`, `createIdentity`, etc.) are **blocking**. Always call them from a background thread (coroutine with `Dispatchers.IO`, `AsyncTask`, `ExecutorService`, etc.). Calling from the main thread will cause an ANR.

### Identity Lifecycle
- **Send as user:** No setup needed beyond the permission grant. The Messenger app must have a logged-in user.
- **Isolated identity:** No permission needed. Call `createIdentity()` once. The identity persists across app restarts, Messenger restarts, and even app reinstalls (as long as the same signing key is used). Subsequent calls to `createIdentity()` return the same address.

### Error Handling
- Methods return `null` on failure (network errors, client not ready, etc.)
- For `SEND_MESSAGE_AS_USER`: a `SecurityException` is thrown if the permission hasn't been granted
- The `isClientReady()` method can be polled to check if the Messenger's XMTP client is available

### Service Binding
- Always set `setPackage("org.ethereumhpone.messenger")` on the intent to ensure you bind to the correct app
- Use `Context.BIND_AUTO_CREATE` to keep the service alive while your app is connected
- Always call `unbindService()` in `onDestroy()` to avoid leaks

### Messenger App Requirements
- The Messenger app must be installed on the device
- For `SEND_MESSAGE_AS_USER`: the user must have completed XMTP onboarding (identity generated)
- For the isolated-identity service: the Messenger app must be installed, but the user does not need to be logged in

### Build Configuration
Your app's `build.gradle.kts` must have AIDL enabled:

```kotlin
android {
    buildFeatures {
        aidl = true
    }
}
```
