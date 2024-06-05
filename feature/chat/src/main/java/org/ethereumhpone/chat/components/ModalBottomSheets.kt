package org.ethereumhpone.chat.components

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.R
import org.ethereumhpone.database.model.Contact
import org.ethosmobile.components.library.core.ethOSIconButton
import org.ethosmobile.components.library.haptics.EthOSHaptics
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@Composable
fun ContactSheet(
    contact: Contact,
    ens: List<String> = emptyList()
){
    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            //.background(Color(0xFF262626))
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
            ){
                if (contact.photoUri != null && contact.photoUri != "") {
                    Image(
                        painter = rememberAsyncImagePainter(contact.photoUri),
                        contentDescription = "Contact Profile Pic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else{
                    Image(painter = painterResource(id = R.drawable.nouns_placeholder), contentDescription = "contact Profile Pic" )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = contact.name,
                    fontSize = 24.sp,
                    color = Colors.WHITE,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = getEnsAddresses(ens),
                    fontSize = 18.sp,
                    color = Colors.GRAY,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))


        Column (
            verticalArrangement = Arrangement.spacedBy(12.dp),

            ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val currentContext = LocalContext.current

                ethOSIconButton(
                    onClick = {
                        // Call contact
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.getDefaultNumber()?.address}")
                        }
                        currentContext.startActivity(intent)
                              },
                    icon = Icons.Outlined.Call,
                    contentDescription="Call"
                )
                ethOSIconButton(
                    onClick = {
                        getContactIdFromPhoneNumber(currentContext, contact.getDefaultNumber()?.address ?: "")?.let {
                            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, it)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = uri
                            }
                            currentContext.startActivity(intent)
                        }
                    },
                    icon = Icons.Outlined.Contacts,
                    contentDescription="Contact"
                )

            }
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = Colors.DARK_GRAY
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
            ) {

                ContactItem(
                    title= "Phone Number",
                    detail= contact.getDefaultNumber()?.address ?: "No Phone Number"
                )

                ContactItem(
                    title= "ENS",
                    detail= getEnsAddresses(ens)
                )

            }


        }
    }
}

fun getContactIdFromPhoneNumber(context: Context, phoneNumber: String): String? {
    val uri: Uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )

    val projection = arrayOf(ContactsContract.PhoneLookup._ID)
    var contactId: String? = null

    val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
        }
    }

    return contactId
}

@Composable
@Preview
fun ContactSheetPreview(){
    //ContactSheet(name = "Mark Katakowski", ens = listOf("mk.eth"))
}

@Composable
fun AssetPickerSheet(
    //balancesState: AssetUiState,
    //getContacts: (Context) -> Unit,
//    assets: AssetUiState,
//    onChangeAssetClicked: (TokenAsset) -> Unit, //method, when asset is selected
//    chainId: INTER
    //swapTokenUiState: SwapTokenUiState,
    //searchQuery: String,
    //onQueryChange: (String) -> Unit,
    //onSelectAsset: (TokenAsset) -> Unit
) {


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            //.background(Color(0xFF262626))
            .padding(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Assets",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
            ) {

                //Mockdata
                ethOSListItem(
                    header = "ETH",
                    withSubheader = true,
                    subheader = "Ether",
                    trailingContent = {
                        //contact.
                        Text(
                            text = "0.8",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Fonts.INTER,
                            color = Color.White
                        )
                    },
                    onClick = { }
                )
                ethOSListItem(
                    header = "DAI",
                    withSubheader = true,
                    subheader = "Dai",
                    trailingContent = {
                        //contact.
                        Text(
                            text = "25",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Fonts.INTER,
                            color = Color.White
                        )
                    },
                    onClick = { }
                )
                ethOSListItem(
                    header = "ARB",
                    withSubheader = true,
                    subheader = "Arbitrum",
                    trailingContent = {
                        //contact.
                        Text(
                            text = "0.2",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Fonts.INTER,
                            color = Color.White
                        )
                    },
                    onClick = { }
                )

            }


        }
    }





}


@Composable
fun ethOSListItem(
    withImage:Boolean = false,
    image: @Composable () -> Unit = {},
    header: String = "Header",
    withSubheader: Boolean = false,
    subheader: String = "Subheader",
    trailingContent: @Composable (() -> Unit)? = null,
    backgroundColor: Color = Colors.TRANSPARENT,
    colorOnBackground: Color = Colors.WHITE,
    subheaderColorOnBackground: Color = Colors.WHITE,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier

) {


    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() },
    ) {
        if(withImage){
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .size(56.dp)
            ) {
                image()
            }
        }

        ListItem(
            headlineContent = {
                Text(
                    text = header,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Colors.WHITE,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.width(160.dp)
                )
            },
            supportingContent = {
                if(withSubheader){
                    Text(
                        text = subheader,
                        color = Colors.GRAY,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.width(160.dp)
                    )
                }

            },
            trailingContent = trailingContent,

            colors = ListItemDefaults.colors(
                headlineColor = colorOnBackground,
                supportingColor = subheaderColorOnBackground,
                containerColor = backgroundColor
            )
        )

    }

}


@Composable
fun ContactItem(title: String, detail: String){//, view: View){
    val interactionSource = remember { MutableInteractionSource() }
    //when user hovers over ContactListItem
    val isHover by interactionSource.collectIsHoveredAsState()
    val context = LocalContext.current

    val urlIntent = when(title){
        "ENS" -> Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://etherscan.io/name-lookup-search?id=${detail}"
            )
        )

        "Ethereum Address" -> Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://etherscan.io/address/${detail}"
            )
        )
        else -> null

    }

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (urlIntent !== null) {
                    //view.performHapticFeedback(EthOSHaptics().NEUTRAL_HAPTIC)
                    context.startActivity(urlIntent)
                }
            }
            .background(
                color = if (isHover) {
                    Colors.DARK_GRAY
                } else {
                    Colors.TRANSPARENT
                }
            )
            .padding(vertical = 12.dp, horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically

    ){

        Column (
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                fontFamily = Fonts.INTER
            )

            Text(
                text = detail,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontFamily = Fonts.INTER
            )
        }
    }
}



@Composable
@Preview
fun AssetPickerSheetPreview(){
    AssetPickerSheet(

//        AssetUiState.Success(
//            listOf(
//                TokenAsset(
//                    chainId = 10,
//                    name = "Optimism",
//                    symbol = "ETH",
//                    balance = 0.23,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//                TokenAsset(
//                    chainId = 1,
//                    name = "Mainnet",
//                    symbol = "ETH",
//                    balance = 1.43,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//                TokenAsset(
//                    chainId = 1,
//                    name = "DAI",
//                    symbol = "ETH",
//                    balance = 123.0,
//                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
//                ),
//            )
//        )
//        ,
//        {},
//        1
    )
}