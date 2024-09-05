package org.ethereumhpone.chat.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.MotionLayout
import androidx.constraintlayout.compose.Transition
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.R
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient
import org.ethosmobile.components.library.core.ethOSIconButton
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import org.ethereumhpone.chat.BuildConfig
import org.kethereum.rpc.HttpEthereumRPC

import java.util.concurrent.CompletableFuture


@Composable
fun ChatHeader(
    modifier: Modifier = Modifier,
    name: String,
    ens: List<String>,
    image: String,
    selectMode: MutableState<Boolean>,
    selectAll: MutableState<Boolean>,
    onBackClick: () -> Unit = {},
    onPhoneClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    onSelectAll: () -> Unit = {},
){
    val iconsize = 24.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = selectMode.value, label = "",
        ){targetMode ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {


                if(targetMode) {
                    TextButton(
                        onClick = onSelectAll,
                        modifier = modifier
                            .height(42.dp)
                            ,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.TRANSPARENT,
                            contentColor = Colors.WHITE,
                        )
                    ) {
                        Text(
                            text = if(selectAll.value) { "Deselect All" } else {  "Select All"},
                            fontSize = 16.sp,
                            fontFamily = Fonts.INTER,
                            color = Colors.WHITE,
                        )
                    }

                    TextButton(
                        onClick = {
                            selectMode.value = false
                        },
                        modifier = modifier
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Colors.TRANSPARENT,
                            contentColor = Colors.WHITE,
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontFamily = Fonts.INTER,
                            color = Colors.WHITE,
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = modifier.size(iconsize)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Go back",
                                tint =  Colors.WHITE,
                                modifier = modifier.size(iconsize)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.clickable { onContactClick()  }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF262626))
                                    .clickable { onContactClick() }
                            ){
                                if (image != ""){
                                    Image(
                                        painter = rememberAsyncImagePainter(image),
                                        contentDescription = "Contact Profile Pic",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                                }
                            }

                            if (ens.isNotEmpty()) {
                                Column (
                                    verticalArrangement = Arrangement.Center,
                                ){
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = if (isEthereumAddress(name)) trimEthereumAddress(name) else name,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = Fonts.INTER,
                                    )
                                }
                            }
                            else {
                                Column {
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = if (isEthereumAddress(name)) trimEthereumAddress(name) else name,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = Fonts.INTER,
                                    )
                                    //val enss = getEnsAddresses(ens)
                                    Text(
                                        textAlign = TextAlign.Center,
                                        text = "",//enss,
                                        fontSize = 14.sp,
                                        color = Colors.GRAY,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = Fonts.INTER,
                                    )
                                }
                            }
                        }
                    }

//      Warning or info
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            IconButton(
                                onClick = onPhoneClick,
                                modifier = modifier.size(iconsize)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phone,
                                    contentDescription = "Go back",
                                    tint =  Colors.WHITE,
                                    modifier = modifier.size(iconsize)
                                )
                            }
                        }
                    }
                }




            }

        }
    }

}

fun getEnsAddresses(ens: List<String>): String{
    var res = ""

    ens.forEachIndexed{ index, value ->
        res += value
        if (index < ens.size-1){
            res += ", "
        }
    }
    return res
}

fun trimEthereumAddress(address: String): String {
    return address.substring(0, 5) + "..." + address.substring(address.length - 3)
}

fun isEthereumAddress(address: String): Boolean {
    return address.startsWith("0x") && address.length == 42
}

fun makePhoneCall(context: Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse("tel:$phoneNumber")
    try {
        context.startActivity(intent)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
    }
}

fun launchApp(context: Context, packageName: String) {
    val pm: PackageManager = context.packageManager
    try {
        val intent: Intent? = pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error launching app", Toast.LENGTH_SHORT).show()
    }
}


@Composable
@Preview
fun PreviewHeader() {

}

