package org.ethereumhpone.messenger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.core.terminalsdk.ReflectiveLedManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ethereumhpone.data.manager.KeyUtil
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.datastore.MessengerPreferences
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.messenger.ui.MessagingApp
import org.ethereumhpone.messenger.ui.rememberMessengerAppState
import org.ethereumhpone.messenger.ui.theme.MessengerTheme
import org.ethereumphone.walletsdk.WalletSDK
import com.messenger.terminalsdk.TerminalLEDController
import kotlinx.coroutines.delay
import org.ethereumphone.dgenlibrary.SystemColorManager
import javax.inject.Inject


private val contactsURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var syncLogDao: SyncLogDao
    @Inject lateinit var logTimeHandler: LogTimeHandler
    @Inject lateinit var privateKeyHandler: XMTPPrivateKeyHandler
    @Inject lateinit var walletSDK: WalletSDK
    @Inject lateinit var xmtpClientManager: XmtpClientManager
    @Inject lateinit var networkManager: NetworkManager
    @Inject lateinit var messengerPreferences: MessengerPreferences


    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncContacts()
            }
        }
    }


    private val viewModel: MainActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize TerminalLEDController
        TerminalLEDController.initialize(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Hide the status bar
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            // If you also want to hide the navigation bar:
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }

        enableEdgeToEdge()
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)


        //update ui state

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        // Sync contacts
        CoroutineScope(Dispatchers.IO).launch {
            syncRepository.syncContacts()
        }

        //TODO: Remove when XMTP implementation is ready



        val threadId = if (intent.getIntExtra("threadId", -1) != -1) {
            intent.getIntExtra("threadId", -1)
        } else {
            null
        }

        if(!permissionManager.isDefaultSms()) {
            val roleManager = this.getSystemService(RoleManager::class.java) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            this.startActivityForResult(intent, 42389)
        }

        // checks if android db contacts have been changed and adds them to the database
        if (permissionManager.hasContacts()) {
            contentResolver.registerContentObserver(contactsURI, true, contentObserver)
        }


        // check if it has permissions and never never ran a message sync
        CoroutineScope(Dispatchers.IO).launch {
            val lastSync = logTimeHandler.getLastLog()
            Log.d("Last sync", lastSync.toString())

            val address = walletSDK.getAddress()

            val keyManager = KeyUtil(this@MainActivity)
            val keys = keyManager.retrieveKey(address)

            Log.d("walletSDK", "current address: $address")

            // Only create XMTP client if user has completed onboarding and chosen to use XMTP
            val preferences = messengerPreferences.prefs.first()
            if (preferences.shouldHideOnboarding && preferences.useXmtp) {
                xmtpClientManager.createClient(walletSDK , this@MainActivity)
            }

            /* comment out for now
            if (keys != null) {
                xmtpClientManager.createClient(walletSDK , this@MainActivity)

                // Start XMTP stream service if user has XMTP enabled
                val preferences = messengerPreferences.prefs.first()
                if (preferences.useXmtp) {
                    // Create notification channel first


                    /*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            "xmtp_channel",
                            "XMTP Message Stream",
                            NotificationManager.IMPORTANCE_LOW
                        ).apply {
                            description = "Listens for new XMTP messages"
                            setShowBadge(false)
                        }
                        val notificationManager = getSystemService(NotificationManager::class.java)
                        notificationManager.createNotificationChannel(channel)
                    }

                    // Start the service
                    val serviceIntent = Intent(this@MainActivity, XmtpMessageStreamService::class.java)
                    //ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                     */



                    Log.d("MainActivity", "Started XMTP stream service")
                }
            }
             */




            //TODO: Remove when everyone is on the new messenger version
            if((lastSync == 0L || lastSync <= 1727630355723) && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
                //syncRepository.syncMessages()
            }
            try {

                //syncRepository.syncXmtp()
                syncRepository.startStream()
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

        var inputAddress: String? = null

        var contactName: String? = null


        val data = intent?.data

        if (data != null) {
            val scheme = data.scheme
            when (scheme) {
                "sms", "smsto", "mms", "mmsto" -> {
                    inputAddress = data.schemeSpecificPart
                    // Remove any query parameters if present
                    inputAddress = inputAddress?.substringBefore('?')
                }
                "ethos-messenger" -> {
                    // Handle deep link from contacts app
                    if (data.host == "chat" && data.pathSegments.firstOrNull() == "new") {
                        inputAddress = data.getQueryParameter("address")
                        contactName = data.getQueryParameter("name")
                    }
                }
            }
        }


        setContent {
            MessengerTheme {
                val appState = rememberMessengerAppState(
                    messengerPreferences = messengerPreferences,
                )

                MessagingApp(
                    messengerAppState = appState,
                    threadId = threadId,
                    inputAddress = inputAddress,
                    contactName = contactName
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
        // Clean up terminal resources when the activity is destroyed
        TerminalLEDController.cleanupSync()
    }

    override fun onPause() {
        super.onPause()
        // Only clear LED, don't cleanup everything
        //TerminalLEDController.clearLED()
    }

    override fun onResume() {
        super.onResume()
        // Re-display chad pattern when app comes back
        SystemColorManager.refresh(this)
        val reflectiveLedManager = ReflectiveLedManager()

        CoroutineScope(Dispatchers.IO).launch {
            while (reflectiveLedManager.isRunning()) {
                delay(50)
            }

            delay(125)
            TerminalLEDController.displayChadPattern()
        }
    }
}
