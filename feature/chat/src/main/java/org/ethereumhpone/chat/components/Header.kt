package org.ethereumhpone.chat.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.platform.LocalContext



@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatHeader(
    modifier: Modifier = Modifier,
    name: String,
    image: String?,
    phoneNumber: List<PhoneNumber>?,
    onBackClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    profileview: MutableState<Boolean>,
    isTrailContent: Boolean = false,
    trailContent: @Composable () -> Unit = {},
){

    if (image != null) {
        Log.d("DEBUG",image)
    }else {
        Log.d("DEBUG","image not working")
    }

    val iconsize = 24.dp

    val context = LocalContext.current


    val profileAnimationProgress by animateFloatAsState(

        // specifying target value on below line.
        targetValue = if (profileview.value) 1f else 0f,

        // on below line we are specifying
        // animation specific duration's 1 sec
        animationSpec = tween(1000)
    )

    MotionLayout(
        ConstraintSet(
            """ {
                
                back_btn: {
                  width: 48,
                  height: 48,
                  start: ['parent', 'start', 16],
                  top: ['parent', 'top', 16]
                },
                profile_pic: {
                  width: 48,
                  height: 48,
                  start: ['back_btn', 'end', 16],
                  top: ['parent', 'top', 16]
                },
                  name: {
                    top: ['profile_pic', 'top'],
                    bottom: ['profile_pic', 'bottom'],
                    start: ['profile_pic', 'end', 16],
                    custom: {
                      size: 18
                    }
                      
                },
                box: {
                  width: "spread",
                  height: "spread",
                  start: ['parent', 'start'],
                  end: ['parent', 'end'],
                  top: ['parent', 'top'],
                  bottom: ['parent', 'bottom',-16],
                }
            } """
        ),

        ConstraintSet(
            """ {
                back_btn: {
                  width: 48,
                  height: 48,
                  start: ['parent', 'start', 16],
                  top: ['parent', 'top', 16]
                },
              profile_pic: {
                  width: 120,
                  height: 120,
                  start: ['parent', 'start'],
                  end: ['parent', 'end'],
                  top: ['back_btn', 'bottom', 48],
                  
                },
                  name: {
                  width: "spread",
                  height: 46,
                    top: ['profile_pic', 'bottom',16],
                    bottom: ['parent', 'bottom',16],
                    end: ['parent', 'end'],
                    start: ['parent', 'start'],
                    custom: {
                      size: 28
                    }
                      
                },
                box: {
                  width: "spread",
                  height: "spread",
                  start: ['parent', 'start'],
                  end: ['parent', 'end'],
                  top: ['parent', 'top'],
                  bottom: ['parent', 'bottom'],
                },
                
              
            } """
        ),


        progress = profileAnimationProgress,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()

    ){

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Colors.BLACK)
                .layoutId("box")
        )


        IconButton(
                    onClick = {
                        if (profileview.value){
                            profileview.value = !profileview.value
                        }else{
                            onBackClick()
                        }

                    },
                    modifier = Modifier
                        .size(48.dp)
                        .layoutId("back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint =  Colors.WHITE,
                        modifier = modifier.size(iconsize)
                    )
                }


                    if (image != "" && !(image.isNullOrEmpty())){
                        Image(
                            painter = rememberAsyncImagePainter(image),
                            contentDescription = "profile_image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF262626))
                                .clickable {
                                    onContactClick()
                                }
                                .layoutId("profile_pic")
                        )
                    } else{
                        Image(
                            painter = painterResource(id = R.drawable.nouns_placeholder),
                            contentDescription = "placeholder_profile_image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262626))
                                .clickable {
                                    onContactClick()
                                }
                                .layoutId("profile_pic")
                        )
                    }




                    val name_properties = motionProperties(id = "name")
                    //with(sharedTransitionScope) {
                    Text(

                        textAlign = TextAlign.Center,
                        text = name,
                        fontSize = name_properties.value.fontSize("size"),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Fonts.INTER,
                        modifier = Modifier
                            .clickable {
                                onContactClick()
                            }
                            .layoutId("name")
                    )
                    // }



    }
                            AnimatedVisibility(
                                profileview.value,
                            enter = fadeIn(
                                animationSpec = tween(300,300),
                            ),
                            exit = fadeOut(
                                animationSpec = tween(300,),
                            )
                        ){
                                Column (
                                    modifier = modifier.padding(start = 24.dp,end = 24.dp,top=48.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ){
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {

                                        ethOSIconButton(
                                            onClick = {
                                                if (phoneNumber != null) {
                                                    makePhoneCall(context, phoneNumber[0].address)
                                                }
                                            },
                                            icon = Icons.Outlined.Call,
                                            contentDescription="Call"
                                        )
                                        ethOSIconButton(
                                            onClick = { /*TODO*/ },
                                            icon = Icons.Outlined.Contacts,
                                            contentDescription="Contact"
                                        )

                                    }
                                    Divider(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Colors.DARK_GRAY
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))
                                    Column(
                                    ) {

                                        phoneNumber?.get(0)?.let {
                                            ContactItem(
                                                title= "Phone Number",
                                                detail= it.address
                                            )
                                        }

                                        //TODO: ENS
//                                        ContactItem(
//                                            title= "ENS",
//                                            detail= getEnsAddresses(ens)
//                                        )

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

