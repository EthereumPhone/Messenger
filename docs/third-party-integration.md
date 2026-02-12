# Third-Party XMTP Messaging Integration Guide

This document explains how third-party Android apps can integrate with the XMTP Messenger app to send messages. Two permissions are available, each unlocking a different level of access.

---

## Overview

| Permission | Purpose | Identity used |
|---|---|---|
| `SEND_MESSAGE_AS_USER` | Send messages as the Messenger's logged-in user | The user's own XMTP identity |
| `GENERATE_XMTP_IDENTITY` | Create an isolated XMTP identity and send from it | A new, app-specific identity |

Both permissions use Android's standard runtime permission flow (dangerous protection level) and expose bound AIDL services that your app connects to via IPC.

---

## Architecture

```
┌─────────────────────┐              ┌────────────────────────────────┐
│  Your App            │              │  XMTP Messenger                │
│                      │              │                                │
│ 1. <uses-permission> │              │ 1. <permission> defined        │
│ 2. requestPerms()    │── Dialog ──▶ │ 2. Android shows grant dialog  │
│ 3. bindService()     │── IPC ────▶ │ 3. Bound AIDL Service          │
│ 4. sendMessage()     │── IPC ────▶ │ 4. checkCallingPermission()    │
│                      │              │ 5. Sends via XMTP protocol     │
└─────────────────────┘              └────────────────────────────────┘
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

## Permission 2: Isolated XMTP Identity

Use this when your app needs **its own XMTP identity** that is separate from the user. Messages are sent from a new Ethereum address that is unique to your app. The user's own identity is never used.

This is ideal for:
- Bots or automated agents that need their own presence on XMTP
- Apps that want to send/receive messages without impersonating the user
- Multi-tenant scenarios where each app has its own messaging identity

### Step 1: Declare the permission

```xml
<uses-permission android:name="org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY" />
```

### Step 2: Copy the AIDL interface

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

### Step 3: Request the permission at runtime

```kotlin
private const val GENERATE_IDENTITY_PERMISSION =
    "org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY"
private const val REQUEST_CODE = 1002

if (checkSelfPermission(GENERATE_IDENTITY_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(GENERATE_IDENTITY_PERMISSION),
        REQUEST_CODE
    )
}
```

### Step 4: Bind to the identity service

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

### Step 5: Create an identity and send messages

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

### Step 6: Unbind when done

```kotlin
override fun onDestroy() {
    super.onDestroy()
    unbindService(identityConnection)
}
```

---

## Complete Example: Activity with Both Permissions

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

        // Request both permissions
        val permsNeeded = mutableListOf<String>()
        if (checkSelfPermission(PERM_SEND) != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(PERM_SEND)
        }
        if (checkSelfPermission(PERM_IDENTITY) != PackageManager.PERMISSION_GRANTED) {
            permsNeeded.add(PERM_IDENTITY)
        }
        if (permsNeeded.isNotEmpty()) {
            requestPermissions(permsNeeded.toTypedArray(), 100)
        } else {
            bindServices()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            bindServices()
        }
    }

    private fun bindServices() {
        // Bind messaging service
        val msgIntent = Intent("org.ethereumhpone.messenger.action.BIND_MESSAGING")
            .setPackage("org.ethereumhpone.messenger")
        bindService(msgIntent, messagingConnection, Context.BIND_AUTO_CREATE)

        // Bind identity service
        val idIntent = Intent("org.ethereumhpone.messenger.action.BIND_IDENTITY")
            .setPackage("org.ethereumhpone.messenger")
        bindService(idIntent, identityConnection, Context.BIND_AUTO_CREATE)
    }

    // Example: send as the logged-in user
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

    // Example: send from isolated identity
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
        private const val PERM_IDENTITY =
            "org.ethereumhpone.messenger.permission.GENERATE_XMTP_IDENTITY"
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
- **Send as user:** No setup needed beyond the permission. The Messenger app must have a logged-in user.
- **Isolated identity:** Call `createIdentity()` once. The identity persists across app restarts and Messenger restarts. Subsequent calls to `createIdentity()` return the same address.

### Error Handling
- Methods return `null` on failure (network errors, client not ready, etc.)
- A `SecurityException` is thrown if the permission hasn't been granted
- The `isClientReady()` method can be polled to check if the Messenger's XMTP client is available

### Service Binding
- Always set `setPackage("org.ethereumhpone.messenger")` on the intent to ensure you bind to the correct app
- Use `Context.BIND_AUTO_CREATE` to keep the service alive while your app is connected
- Always call `unbindService()` in `onDestroy()` to avoid leaks

### Messenger App Requirements
- The Messenger app must be installed on the device
- For `SEND_MESSAGE_AS_USER`: the user must have completed XMTP onboarding (identity generated)
- For `GENERATE_XMTP_IDENTITY`: the Messenger app must be installed, but the user does not need to be logged in

### Build Configuration
Your app's `build.gradle.kts` must have AIDL enabled:

```kotlin
android {
    buildFeatures {
        aidl = true
    }
}
```
