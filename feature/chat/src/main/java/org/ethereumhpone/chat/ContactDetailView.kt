package org.ethereumhpone.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.components.ContactItem
import org.ethereumhpone.chat.components.makePhoneCall
import org.ethereumhpone.database.model.Recipient
import org.ethosmobile.components.library.core.ethOSIconButton
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun ContactDetailView(
    modifier: Modifier = Modifier,
    profileview: MutableState<Boolean>,
    name: String,
    ens: List<String>,
    image: String,
    recipient: Recipient?,
    onMembersClick: () -> Unit = {},
    onMediaClick: () -> Unit = {},
    onTxClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
){

    val context = LocalContext.current
    val alpha1: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,500))
    val alpha2: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,500))
    val alpha3: Float by animateFloatAsState(if (profileview.value) 1f else 0.0f, animationSpec = tween(1000,500))

    Box(modifier = modifier
        .fillMaxSize()
        .background(Colors.BLACK)
    ) {
        //------------PROFILE VIEW START------------

        Column (
            modifier = modifier

                .fillMaxHeight()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                //.padding(start = 24.dp, end = 24.dp, top = 12.dp),
                horizontalArrangement = Arrangement.Start,
            ) {


                IconButton(
                    onClick = { profileview.value = false },
                    modifier = modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint = Colors.WHITE,
                        modifier = modifier.size(24.dp)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)

                ){

                    if (image != ""){
                        Image(
                            painter = rememberAsyncImagePainter(image),
                            contentDescription = "Contact Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else{
                        Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                    }
                }
                Text(
                    textAlign = TextAlign.Center,
                    text = name,
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.INTER,
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.graphicsLayer(alpha = alpha1),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {

                    Box(modifier = Modifier
                        .graphicsLayer(alpha = alpha1)
                        .padding(end = 12.dp)) {
                        ethOSIconButton(
                            onClick = {
                                if (recipient?.contact?.numbers?.get(0)  != null) {
                                    makePhoneCall(context, recipient.contact?.numbers?.get(0)!!.address)
                                }
                            },
                            icon = Icons.Outlined.Call,
                            contentDescription="Call"
                        )
                    }
                    Box(modifier = Modifier.graphicsLayer(alpha = alpha2)) {
                        ethOSIconButton(
                            onClick = onContactClick,
                            icon = Icons.Outlined.Contacts,
                            contentDescription="Contact"
                        )
                    }



                }
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(alpha = alpha3),
                    color = Colors.DARK_GRAY
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),

                ) {
                ProfileDetailItem(
                    title = "Media",
                    icon = Icons.Outlined.PermMedia,
                    onClick = onMediaClick,
                )

                ProfileDetailItem(
                    title = "Transaction",
                    icon = Icons.Outlined.AttachMoney,
                    onClick = onTxClick,
                )

                //TODO: Members
//                ProfileDetailItem(
//                    title = "Members",
//                    icon = Icons.Outlined.Person,
//                    onClick = onMembersClick,
//                )
            }


            recipient?.contact?.numbers?.get(0).let {
                if (it != null) {
                    ContactItem(
                        modifier = Modifier,//.graphicsLayer(alpha = alpha4),
                        title= "Phone Number",
                        detail= it.address
                    )
                }
            }

            recipient?.contact?.ethAddress.let {
                if (it != null && it.isNotBlank()) {
                    ContactItem(
                        modifier = Modifier,//.graphicsLayer(alpha = alpha5),
                        title= "Ethereum Address",
                        detail= it
                    )
                }
            }

        }

    }
}

@Composable
fun ProfileDetailItem(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {},
    icon: ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical=12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = "Icon",
                tint = Colors.WHITE
            )
            Text(
                title,
                color = Colors.WHITE,
                fontFamily = Fonts.INTER,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        IconButton(
            onClick = onClick,
            modifier = modifier.size(18.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "Go back",
                tint = Colors.WHITE,
                modifier = modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun DetailContactItem(
    modifier: Modifier = Modifier,
){
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
        ,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .size(48.dp)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.nouns_placeholder),
                    contentDescription = "Contact Image",
                    modifier = Modifier
                        .size(48.dp) // Set the size of the image
                        .clip(CircleShape) // Apply a circular shape
                )
            }
            Column (
                modifier = modifier.weight(1f)
            ){
                Text(
                    text = "Elie Munsi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Fonts.INTER,
                    color = Colors.WHITE,
                )
                Text(
                    text = "+437894561230",
                    style = TextStyle(
                        color = Colors.GRAY,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Fonts.INTER,
                    ),
                )
            }

            if (false){
                Text(
                    text = "Admin",
                    style = TextStyle(
                        color = Colors.GRAY,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = Fonts.INTER,
                    )
                )
            }

        }
    }
}
