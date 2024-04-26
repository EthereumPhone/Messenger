package org.ethereumhpone.messenger

import android.os.Bundle
import android.provider.ContactsContract
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ethereumhpone.data.observer.observe
import org.ethereumhpone.database.dao.SyncLogDao
import org.ethereumhpone.domain.manager.PermissionManager
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // checks if android db contacts have been changed and adds them to the database
        contentResolver.observe(contactsURI).map {
            Log.d("Resolver", "Got trigger")
            syncRepository.syncContacts()
        }


        if (permissionManager.hasContacts()) {
            CoroutineScope(Dispatchers.IO).launch {
                syncRepository.syncContacts()
            }
        }

        // check if it has permissions and never never ran a message sync
        CoroutineScope(Dispatchers.IO).launch {
            val lastSync = syncLogDao.getLastLogDate() ?: 0
            if(lastSync == 0L && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
                syncRepository.syncMessages()
            }
        }



        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);




            setContent {
                MessengerTheme {
//                    ContactPermissionExample {
                        MessagingApp()
//                    }
                }
            }


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