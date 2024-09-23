package org.ethereumhpone.messenger

import android.app.role.RoleManager
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Base64.NO_WRAP
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ethereumhpone.data.manager.EthOSSigningKey
import org.ethereumhpone.data.manager.KeyUtil
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.domain.manager.NetworkManager
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.model.XMTPPrivateKeyHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.messenger.ui.MessagingApp
import org.ethereumhpone.messenger.ui.theme.MessengerTheme
import org.ethereumphone.walletsdk.WalletSDK
import org.xmtp.android.library.Client
import org.xmtp.android.library.messages.PrivateKeyBundleV1Builder
import javax.inject.Inject
import kotlin.io.encoding.Base64.Default.encode


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

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        //update ui state

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        val keyManager = KeyUtil(this@MainActivity)
        var keys = keyManager.retrieveKey(walletSDK.getAddress())

        if (keys == null) {
            val context = this@MainActivity
            runBlocking {
                Client().create(
                    EthOSSigningKey(walletSDK),
                    XmtpClientManager.clientOptions(context, walletSDK.getAddress())
                ).apply {
                    keyManager.storeKey(walletSDK.getAddress(), PrivateKeyBundleV1Builder.encodeData(privateKeyBundleV1))
                    keys = PrivateKeyBundleV1Builder.encodeData(privateKeyBundleV1)
                }

            }
        }

        xmtpClientManager.createClient(keys!! , this@MainActivity)


        val threadId = if (intent.getIntExtra("threadId", -1) != -1) {
            intent.getIntExtra("threadId", -1)
        } else {
            null
        }

        if(!permissionManager.isDefaultSms()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = this.getSystemService(RoleManager::class.java) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                this.startActivityForResult(intent, 42389)
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, this.packageName)
                this.startActivity(intent)
            }
        }

        // checks if android db contacts have been changed and adds them to the database
        if (permissionManager.hasContacts()) {
            contentResolver.registerContentObserver(contactsURI, true, contentObserver)
        }


        // check if it has permissions and never never ran a message sync
        CoroutineScope(Dispatchers.IO).launch {
            val lastSync = logTimeHandler.getLastLog()
            if(lastSync == 0L && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
                syncRepository.syncMessages()
            }

            // Suspend until clientState is Ready
            xmtpClientManager.clientState.first { it == XmtpClientManager.ClientState.Ready }

            // Now clientState is Ready, proceed to call syncXmtp
            syncRepository.syncXmtp(context = this@MainActivity, xmtpClientManager.client)

            syncRepository.startStreamAllMessages(xmtpClientManager.client)
        }

        var inputAddress: String? = null

        val data = intent?.data

        if (data != null) {
            val scheme = data.scheme
            if (scheme == "sms" || scheme == "smsto" || scheme == "mms" || scheme == "mmsto") {
                inputAddress = data.schemeSpecificPart
                // Remove any query parameters if present
                inputAddress = inputAddress?.substringBefore('?')
            }
        }


        setContent {
            //TODO: Move all the sync code to workers
            val useXmtp = shouldUseXmtp(uiState)



            MessengerTheme {
                MessagingApp(
                    threadId = threadId,
                    inputAddress = inputAddress
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)

    }
}

@Composable
private fun shouldUseXmtp(
    uiState: MainActivityUiState
): Boolean = when(uiState) {
    MainActivityUiState.Loading -> false
    is MainActivityUiState.Success -> {
        uiState.userData.useXmtp
    }
}