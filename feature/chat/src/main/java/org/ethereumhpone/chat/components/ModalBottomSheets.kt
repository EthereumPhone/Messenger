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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.LocalPhone
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import org.ethereumhpone.chat.MessagesUiState
import org.ethereumhpone.chat.R
import org.ethereumhpone.chat.components.message.parts.getVideoThumbnail
import org.ethereumhpone.chat.extractTransactionDetails
import org.ethereumhpone.chat.isValidTransactionMessage
import org.ethereumhpone.database.model.Contact
import org.ethereumhpone.database.model.Recipient
import org.ethereumhpone.database.model.isSmil
import org.ethereumhpone.database.model.isText
import org.ethereumhpone.database.model.isVideo
import org.ethosmobile.components.library.core.ethOSIconButton
import org.ethosmobile.components.library.haptics.EthOSHaptics
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.walletmanager.ethOSTransferListItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


enum class DetailSelector {
    ASSET,
    CONTACT,
    MEDIA,
    MEMBERS,
    TXS
}

@Composable
fun ContactSheet(
    modifier: Modifier = Modifier,
    name: String,
    recipient: Recipient?,
    image: String,
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
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(214.dp)
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
            ){
                if (image.isNotEmpty() && image.isNotBlank()) {
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
        }





        Spacer(modifier = Modifier.height(36.dp))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Info",
                    color = Colors.WHITE,
                    fontFamily = Fonts.INTER,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )


                ContactInfo(title = name, icon = Icons.Outlined.Person)
                recipient?.contact?.numbers?.get(0)?.let { ContactInfo(title = it.address, icon = Icons.Outlined.LocalPhone) }


                recipient?.contact?.ethAddress.let {
                    if (it != null && it.isNotBlank()) {
                        ContactInfo(title = it, icon = ImageVector.vectorResource(id = R.drawable.ethereum_logo))
                    }
                }
                if(getEnsAddresses(ens).isNotEmpty() || getEnsAddresses(ens).isNotBlank()){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {


                        Text(
                            "ENS",
                            color = Colors.WHITE,
                            fontFamily = Fonts.INTER,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            getEnsAddresses(ens),
                            color = Colors.WHITE,
                            fontFamily = Fonts.INTER,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )

                    }
                }


            }


        }
    }
}

@Composable
fun MediaSheet(
    messagesUiState: MessagesUiState,
    modifier: Modifier = Modifier,
){
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Media",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            when(messagesUiState){
                is MessagesUiState.Loading -> {
                    Text(text = "Loading", color = Colors.WHITE)
                }

                is MessagesUiState.Success -> {
                    val allmedia = messagesUiState.messages.filter { it.parts.isNotEmpty() }

                    if (allmedia.isEmpty()){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Fit,
                                imageVector = Icons.Outlined.PermMedia, //painterResource(id = R.drawable.no_transfer),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)
                            )
                            Text(text = "No Media", color = Colors.GRAY, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                        }
                    }else{
                        LazyVerticalStaggeredGrid(
                            modifier = modifier.fillMaxSize(),
                            columns = StaggeredGridCells.Fixed(2),
                            verticalItemSpacing = 4.dp,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            content = {
                                items(items = allmedia){
                                    val media = remember { it.parts.filter { !it.isText() && !it.isSmil() } }

                                    media.forEachIndexed { index, item ->
                                        Box(Modifier.clip(RoundedCornerShape(15.dp))) {
                                            AsyncImage(
                                                model = if (item.isVideo()) item.getUri().getVideoThumbnail(LocalContext.current) else item.getUri(),
                                                contentDescription = "",
                                                contentScale = ContentScale.Crop,
                                                placeholder = painterResource(id = R.drawable.ethos_placeholder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            32.dp,
                                                            32.dp,
                                                            32.dp,
                                                            32.dp
                                                        )
                                                    )
                                            )

                                            if(item.isVideo()) {
                                                androidx.compose.material.Icon(
                                                    imageVector = Icons.Rounded.PlayArrow,
                                                    contentDescription = "",
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .offset(10.dp, (-10).dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.5f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }




                }
            }
        }
    }
}

@Composable
fun MembersSheet(
    modifier: Modifier = Modifier,
){
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp),
            ) {

            Text(
                text = "Members",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TXSheet(
    messagesUiState: MessagesUiState,
    modifier: Modifier = Modifier,
){
    val context = LocalContext.current
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Transaction",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
            ) {
            when (messagesUiState) {
                MessagesUiState.Loading -> TODO()
                is MessagesUiState.Success -> {
                    val alltxs = messagesUiState.messages.filter { isValidTransactionMessage(it.body) }

                    if(alltxs.isEmpty() ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Fit,
                                painter = painterResource(id = R.drawable.no_transfer),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)
                            )
                            Text(text = "No transfers", color = Colors.GRAY, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                        }
                    }else{
                        LazyColumn(
                            modifier = modifier.fillMaxSize()
                        ) {
                            items(items = alltxs, key = { it.id }) { message ->
                                val transactionDetails = extractTransactionDetails(message.body)
                                val formatedDate = printFormattedDateInfo(Date(message.date))

                                transactionDetails?.let {
                                    if (formatedDate != null) {
                                        ethOSTransferListItem(
                                            asset = "ETH",
                                            value = "${it.amount.toDouble()}",
                                            timeStamp =  formatedDate,
                                            userSent = message.isMe(),
                                            onCardClick = {
                                                //Go to link
                                                // Open link in browser
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.data = Uri.parse(it.url)
                                                startActivity(context, intent, null)
                                            }
                                        )
                                    }
                                }


                            }

                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ContactInfo(
    title: String,
    icon: ImageVector
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        Icon(
            icon,
            contentDescription = "Icon",
            tint = Colors.WHITE,
            modifier = Modifier.size(32.dp),
        )
        Text(
            title,
            color = Colors.WHITE,
            fontFamily = Fonts.INTER,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

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
fun ContactItem(modifier: Modifier=Modifier,title: String, detail: String){//, view: View){
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


fun printFormattedDateInfo(date: Date?): String? {


    val formattedDate = date?.let { formatDate(it) }

    val calendar = Calendar.getInstance()
    if (date != null) {
        calendar.time = date
    }

    val currentCalendar = Calendar.getInstance()

    when {
        date?.let { isWithinLast7Days(it) } == true -> {
            val weekday = getWeekday(date)
            if (isSameDay(calendar, currentCalendar)){
                return formattedDate
            }

            return weekday
        }
        date?.let { isBeforeLast7Days(it) } == true -> {
            println("The date $formattedDate is before the last 7 days.")
            return formattedDate
        }
        else -> {
            println("The date $formattedDate is not within the last 7 days and not before the last 7 days (i.e., it's in the future).")
            return formattedDate
        }
    }
}

fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val currentCalendar = Calendar.getInstance()

    return when {
        isSameDay(calendar, currentCalendar) -> {
            SimpleDateFormat("HH:mm").format(date)
        }
        calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) -> {
            SimpleDateFormat("MM.dd").format(date)
        }
        else -> {
            SimpleDateFormat("yyyy.MM.dd").format(date)
        }
    }
}

fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

fun isWithinLast7Days(date: Date): Boolean {
    val currentDate = Date()
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = currentDate
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return !date.before(sevenDaysAgo) && !date.after(currentDate)
}

fun isBeforeLast7Days(date: Date): Boolean {
    val sevenDaysAgo = Calendar.getInstance().apply {
        time = Date()
        add(Calendar.DAY_OF_YEAR, -7)
    }.time

    return date.before(sevenDaysAgo)
}

fun getWeekday(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
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