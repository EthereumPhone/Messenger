package org.ethereumhpone.messenger

import android.app.role.RoleManager
import android.content.ContentResolver
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ethereumhpone.data.observer.observe
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.domain.manager.PermissionManager
import org.ethereumhpone.domain.model.LogTimeHandler
import org.ethereumhpone.domain.repository.SyncRepository
import org.ethereumhpone.messenger.ui.MessagingApp
import org.ethereumhpone.messenger.ui.theme.MessengerTheme
import javax.inject.Inject


private val contactsURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncRepository: SyncRepository

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var syncLogDao: SyncLogDao

    @Inject
    lateinit var logTimeHandler: LogTimeHandler


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