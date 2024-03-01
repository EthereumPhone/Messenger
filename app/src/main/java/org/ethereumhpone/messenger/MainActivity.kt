package org.ethereumhpone.messenger

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import org.ethereumhpone.messenger.ui.MessagingApp
import org.ethereumhpone.messenger.ui.theme.MessengerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


//@SuppressLint("PermissionLaunchedDuringComposition")
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun ContactPermissionExample(content: @Composable () -> Unit) {
//    val contactsPermissionState = rememberPermissionState(permission = Manifest.permission.READ_CONTACTS)
//
//    when {
//        contactsPermissionState.hasPermission -> {
//            // Permission granted: Show content
//            content()
//        }
//        contactsPermissionState.shouldShowRationale -> {
//            // Explain why the permission is needed and re-request permission
//            // You can use a dialog or a simple Text composable for explanation
//            Text("We need access to your contacts to show them in the app.")
//            // Optionally, add a button or gesture to re-request permission
//        }
//        else -> {
//            // Request permission
//            contactsPermissionState.launchPermissionRequest()
//        }
//    }
//}
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