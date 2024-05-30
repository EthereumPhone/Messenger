package org.ethereumhpone.chat

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.PhoneNumber
import org.ethereumhpone.database.model.Recipient
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileDetailScreen(
    isGroup: Boolean = false,
    modifier: Modifier = Modifier,
    //contacts: List<Contact> = emptyList(),
    recipient: Recipient?,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
    //group: List<Contact> = emptyList()
){
    //ChatScreen(navigateBackToConversations={},chatUIState=ChatUIState.Success(listOf()))


    val contact = recipient?.contact

    var showDialog by remember {
        mutableStateOf(false)
    }



    Column (
        modifier = modifier
            .fillMaxSize()
            .background(Colors.BLACK),
        //verticalArrangement = Arrangement.SpaceBetween
    ){

        //back button(
        ProfileDetailHeader(
            onBackClick = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {


            //Profile image
            Column (
                modifier = modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ){

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262626))
                ){
                    with(sharedTransitionScope) {
                        when(isGroup){
                            false -> {

                                if (contact != null) {
                                    if (contact.photoUri != ""){
                                        Image(
                                            painter = rememberAsyncImagePainter(contact.photoUri),
                                            contentDescription = "Contact Profile Pic",
                                            contentScale = ContentScale.Crop
                                        )
                                    } else{
                                        Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                                    }
                                }
                            }

                            true -> {
                                if (contact != null) {
                                    if (contact.photoUri != ""){
                                        Image(
                                            painter = rememberAsyncImagePainter(contact.photoUri),
                                            contentDescription = "profile_image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .sharedElement(
                                                    rememberSharedContentState(key = "profile_image"),
                                                    animatedVisibilityScope = animatedVisibilityScope
                                                )
                                        )
                                    } else{
                                        Image(
                                            painter = painterResource(id = R.drawable.nouns_placeholder),
                                            contentDescription = "contact Profile Pic",
                                            modifier = Modifier
                                                .sharedElement(
                                                    rememberSharedContentState(key = "placeholder_profile_image"),
                                                    animatedVisibilityScope = animatedVisibilityScope
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                }

                when(isGroup){
                    false -> {

                        if (contact != null) {
                            with(sharedTransitionScope) {
                                Text(
                                    modifier = Modifier
                                        .sharedElement(
                                            rememberSharedContentState(key = "contact_name"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        ),
                                    text = contact.name,
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Colors.WHITE,
                                        fontFamily = Fonts.INTER
                                    )
                                )
                            }

                        }
                    }

                    true -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            with(sharedTransitionScope) {
                                Text(
                                    modifier = Modifier
                                        .sharedElement(
                                            rememberSharedContentState(key = "contact_name"),
                                            animatedVisibilityScope = animatedVisibilityScope
                                        ),
                                    text = "Group Name", // TODO: Implement Group Fucntionality
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Colors.WHITE,
                                        fontFamily = Fonts.INTER
                                    )
                                )
                            }
                            Text(
                                text = "N/A Members", // TODO: Implement Group Fucntionality
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Colors.GRAY,
                                    fontFamily = Fonts.INTER
                                )
                            )

                        }



                    }
                }

            }


            //Action calls (call,text,contact)
            if(!isGroup) {
                Column (
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Row {
                        ContactActionButton(Icons.Outlined.Phone,"Call",{})
                        ContactActionButton(Icons.Outlined.Contacts,"Contact",{})
                    }
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1a1a1a)
                    )
                }
            }

            //phone number/s

            if (!isGroup){
                if (contact != null) {
                    ContactDetail(title = "Phone Number", detail = contact.numbers[0].address)
                }
            }


            //TODO: Implement Media & Tx Section

            Column (
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
//                ExtraDataItem(title = "Media")
//                ExtraDataItem(title = "Media")
//                ExtraDataItem(title = "Media")
            }




            if (isGroup){
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "N/A Members",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Colors.WHITE,
                            fontFamily = Fonts.INTER
                        )
                    )
                    LazyColumn(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
//                        contacts.forEach {
//                            item {
//                                Row (
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                                ){
//                                    Box(
//                                        modifier = Modifier
//                                            .size(52.dp)
//                                            .clip(CircleShape)
//                                            .background(Color(0xFF262626))
//                                    ){
//
//                                    }
//                                    Column (
//                                        verticalArrangement = Arrangement.spacedBy(2.dp),
//                                        modifier= modifier.fillMaxWidth()
//                                    ){
//                                        Text(
//                                            text = it.name,
//                                            fontSize = 18.sp,
//                                            fontWeight = FontWeight.SemiBold,
//                                            color = Color.White
//                                        )
//
//                                        Text(
//                                            text = it.numbers[0].address,
//                                            fontSize = 16.sp,
//                                            fontWeight = FontWeight.Normal,
//                                            color = Color.Gray
//                                        )
//
//
//                                    }
//                                }
//                            }
//                        }

                    }


                    Spacer(modifier = modifier.height(24.dp))

                }
            }


            //delete contqct
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 48.dp)
            ) {
                Button(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor=  Color.White,

                        ),
                    onClick = {
                        showDialog = true
                    }) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = modifier.fillMaxWidth()
                    ){
                        Text(
                            text = "Delete Contact",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Colors.ERROR,
                                fontFamily = Fonts.INTER
                            )
                        )
                        Icon(imageVector = Icons.Outlined.Delete, tint = Colors.ERROR, modifier = modifier
                            .padding(start = 12.dp)
                            .size(20.dp), contentDescription = "Delete Contact")
                    }
                }
            }





        }



    }




    //Media and Tx

    //phone number/s


    //delete contqct
}


@Composable
fun ProfileDetailHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
){
    val iconsize = 24.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 8.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

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


            }
        }

    }
}

//PICL
@Composable
fun ContactActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
){

    Column (
        modifier = Modifier.clickable {
            onClick()
        },
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        IconButton(
            onClick = {
                onClick()
            },
        ) {
            Icon(imageVector = icon, contentDescription = "", modifier = Modifier.size(28.dp), tint = Colors.WHITE)
        }

        Text(
            modifier = Modifier.clickable {
                onClick()
            },
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Colors.WHITE
        )
    }
}

@Composable
fun ContactDetail(
    modifier: Modifier = Modifier,
    title: String,
    detail: String
){
    Column (
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier= modifier.fillMaxWidth()
    ){
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )

        Text(
            text = detail,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun ExtraDataItem(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {}
){
    //TODO: Finish functionality
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()

    ){
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Colors.WHITE,
                fontFamily = Fonts.INTER
            )
        )

        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ){
            Text(
                text = "0",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Colors.WHITE,
                    fontFamily = Fonts.INTER
                )
            )

            IconButton(onClick = onClick , modifier = modifier.size(28.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowForwardIos, tint = Colors.WHITE, modifier = modifier.size(20.dp), contentDescription = "Go forward")
            }


        }
    }
}



@Composable
@Preview(showBackground = true,device = "id:pixel_7a",)
fun PreviewProfileDetailScreen(){
//    ProfileDetailScreen(
//        isGroup = false,
//        contacts = listOf(
//            Contact(
//                name = "Elie",
//                numbers = listOf(
//                    PhoneNumber(
//                        address = "+43123456789"
//                    )
//                )
//            )
//            ,
//            Contact(
//                name = "Elie",
//                numbers = listOf(
//                    PhoneNumber(
//                        address = "+43123456789"
//                    )
//                )
//            )
//
//        ),
//    )
}