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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ethereumhpone.data.manager.EthOSSigningKey
import org.ethereumhpone.data.manager.KeyUtil
import org.ethereumhpone.data.manager.XmtpClientManager
import org.ethereumhpone.database.dao.SyncLogDao
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


    private val contentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncContacts()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val keyManager = KeyUtil(this)


        while (walletSDK.getAddress() == "") {
            "aaa"
        }





        Log.d("MY ADD", walletSDK.getAddress())
        var keys = keyManager.retrieveKey(walletSDK.getAddress())
        Log.d("MY key", keys?: "")

        if (keys == null) {
            val context = this
            //CoroutineScope(Dispatchers.IO).launch {
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

        xmtpClientManager.createClient(keys!! , this)



        lifecycleScope.launch {
            xmtpClientManager.clientState.collectLatest { state ->

                when(state) {
                    is XmtpClientManager.ClientState.Ready -> {
                        Log.d("STATE", "ready")

                    }
                    is XmtpClientManager.ClientState.Unknown -> {
                        Log.d("STATE", "not ready")

                    }
                    is XmtpClientManager.ClientState.Error -> {
                        Log.d("STATE", state.message)

                    }
                }

                if (state == XmtpClientManager.ClientState.Ready) {
                    Log.d("Start service", "IT started")

                    val intent = Intent(this@MainActivity, MyForegroundService::class.java)
                    this@MainActivity.startForegroundService(intent)
                }
            }
        }


        val threadId = if (intent.getIntExtra("threadId", -1) != -1) {
            intent.getIntExtra("threadId", -1)
        } else {
            null
        }



        /*
        if (privateKeyHandler.getPrivate() == null) {
            val context = this
            val options = ClientOptions(api = ClientOptions.Api(env = XMTPEnvironment.PRODUCTION, isSecure = true), appContext = context)
            CoroutineScope(Dispatchers.IO).launch {
                // Create client to save key
                val client = Client().create(account = EthOSSigningKey(walletSDK), options = options)
                privateKeyHandler.setPrivate(PrivateKeyBundleV1Builder.encodeData(client.privateKeyBundleV1))

                // Start foreground service
                val intent = Intent(context, MyForegroundService::class.java)
                context.startForegroundService(intent)
            }
        }
         */



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

        // initial contacts fetching
        /*
        if (permissionManager.hasContacts()) {
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncContacts()
            }
        }
         */

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
        }



        setContent {
            MessengerTheme {
                MessagingApp(threadId = threadId)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MessengerTheme {
        Greeting("Android")
    }
}