package org.ethereumhpone.chat

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.chat.components.TransationLog
import org.ethereumhpone.chat.components.media.MediaThumbnail
import org.ethereumhpone.chat.screen.MediaDetailScreen
import org.ethereumhpone.chat.screen.MediaGridScreen
import org.ethereumhpone.chat.screen.TransactionLogScreen
import org.ethereumhpone.chat.util.abbreviateNumber
import org.ethereumhpone.chat.util.formatAddress
import org.ethereumhpone.chat.util.formatSmart
import org.ethereumhpone.chat.util.generateRandomTransfers
import org.ethereumhpone.chat.util.urisToAttachments
import org.ethereumhpone.domain.model.Attachment
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.verticalLazyListScrollbar
import org.ethereumphone.dgenlibrary.components.verticalScrollBarForLazyGrid
import org.ethereumphone.model.Recipient
import org.ethosmobile.components.library.models.TransferItem
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OverlayContactScreen(
    onBackClick: () -> Unit,
    title: String = "",
    selectedIndex: Int = -1,
    media: List<Uri> = emptyList(),
    transactions: List<TransferItem> = emptyList(),
    recipientUiState: RecipientUiState,
    deleteGroup: () -> Unit = {},
    leaveGroup: () -> Unit = {},
    deleteContact: () -> Unit = {},
    deleteMember: (Recipient?) -> Unit = {},
    next: () -> Unit = {},
    prev: () -> Unit = {},
    select: (Int) -> Unit = {},
    isGroup: Boolean = true
) {

    val context = LocalContext.current
    //TODO: Replace it with real media
    //TODO: Replace it with real transfers

    var showAction by remember { mutableStateOf(false)}
    var showConfirmation by remember { mutableStateOf(false)}
    var action by remember { mutableStateOf(ContactActions.MEDIA)}
    var confirmation by remember { mutableStateOf(ContactConfirmation.LEAVEGROUP)}
    var editMode by remember { mutableStateOf(false)}
    val scrollState = rememberLazyListState()
    val gridscrollState = rememberLazyGridState()

    val members = when(recipientUiState){
        RecipientUiState.Error -> emptyList()
        RecipientUiState.Loading -> emptyList()
        is RecipientUiState.Success -> recipientUiState.recipients
    }


    var selectedMember: Recipient? by remember { mutableStateOf<Recipient?>(null) }
    var openMediaDetail by remember { mutableStateOf(false) }
    var coroutinescope = rememberCoroutineScope()


    val attachments = urisToAttachments(context, media)

    Box(
        Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            targetState = showAction,
            transitionSpec = {
                fadeIn(animationSpec = tween(300, 300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        ) { targetState ->
            if (!targetState){
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .verticalLazyListScrollbar(scrollState,fixed=true)
                        .fillMaxSize()
                        .padding(top = 32.dp, end = 32.dp, start = 32.dp)
                ){
                    item {
                        Column(modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ContactSection(
                                title = "MEDIA",
                                amount = media.size, //TODO: count real media objects
                                onDone = {
                                    action = ContactActions.MEDIA
                                    showAction = true
                                },
                            )
                            ContactSection(
                                title = "TRANSACTIONS",
                                amount = transactions.size, //TODO: count real TXs
                                onDone = {
                                    action = ContactActions.TX
                                    showAction = true
                                },
                            )
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            when(isGroup){
                                true -> {
                                    Column(
                                        modifier = Modifier.padding(top = 0.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text(
                                                text = "MEMBERS",
                                                style = TextStyle(
                                                    fontFamily = SpaceMono,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 20.sp,
                                                    lineHeight = 20.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )

                                            //If user ist Admin
                                            // TODO: Add if user is admin conditinal
                                            /*Text(
                                                modifier = Modifier.pointerInput(Unit){
                                                    detectTapGestures {
                                                        editMode = !editMode
                                                    }
                                                },
                                                text = if(editMode) "SAVE" else "EDIT",
                                                style = TextStyle(
                                                    fontFamily = SpaceMono,
                                                    color = if(editMode) dgenTurqoise else dgenTurqoise.copy(0.7f),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    lineHeight = 14.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )*/
                                        }
                                        Column(
                                            modifier = Modifier.background(dgenBlack),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            members.forEach { member->
                                                MemberItem(
                                                    member.ens.toString(),
                                                    onClick = {
                                                        confirmation = ContactConfirmation.DELETEMEMEBER
                                                        showConfirmation = true
                                                        selectedMember = member
                                                    },
                                                    editMode = editMode,
                                                    isAdmin = false //TODO: add admin value
                                                )
                                            }
                                        }
                                    }
                                    //TODO: if user is admin enable to delete Group
                                    /*Text("DELETE GROUP",
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenRed,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    confirmation =
                                                        ContactConfirmation.DELETEGROUP
                                                    showConfirmation = true
                                                    //onDone()

                                                }
                                            },
                                    )*/

                                    Text("LEAVE GROUP",
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenRed,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    confirmation = ContactConfirmation.LEAVEGROUP
                                                    showConfirmation = true
                                                    //TODO: Add leave group feature
                                                }
                                            },
                                    )
                                }
                                false -> {
                                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "ENS",
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                lineHeight = 16.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None
                                            )
                                        )
                                        Text(
                                            text = members[0].ens.toString(),
                                            style = TextStyle(
                                                fontFamily = PitagonsSans,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 24.sp,
                                                lineHeight = 24.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None
                                            )
                                        )
                                    }
                                    //TODO: Add phone number
                                    /*Column(modifier = Modifier.fillMaxWidth(),verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text= "PHONE NUMBER",
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp,
                                                lineHeight = 16.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None
                                            )
                                        )
                                        Text(
                                            text= members[0].ens.toString(), //TODO: add phone number - currently not possible
                                            style = TextStyle(
                                                fontFamily = PitagonsSans,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 24.sp,
                                                lineHeight = 24.sp,
                                                letterSpacing = 0.sp,
                                                textDecoration = TextDecoration.None
                                            )
                                        )
                                    }*/

                                    Text("DELETE CONTACT",
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenRed,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 24.sp,
                                            lineHeight = 24.sp,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(Unit) {
                                                detectTapGestures {
                                                    confirmation =
                                                        ContactConfirmation.DELETECONTACT
                                                    showConfirmation = true
                                                }
                                            },

                                        )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
            else{
                    // MEDIA or TRANSACTION
                    when(action){
                        ContactActions.MEDIA -> {

                            Crossfade(
                                openMediaDetail
                            ) { openDetail ->
                                if(!openDetail){
                                    /*LazyVerticalGrid(
                                        state = gridscrollState,
                                        modifier = Modifier
                                            .verticalScrollBarForLazyGrid(gridscrollState)
                                            .fillMaxSize()
                                            .padding(horizontal = 32.dp),
                                        columns = GridCells.Fixed(3), // 3 columns
                                        //columns = GridCells.Adaptive(minSize = 120.dp), // Minimum size of each card
                                        contentPadding = PaddingValues(20.dp), // Padding around the grid
                                        horizontalArrangement = Arrangement.spacedBy(20.dp), // Horizontal spacing between cards
                                        verticalArrangement = Arrangement.spacedBy(20.dp)
                                    ) {
                                        //TODO: Add Images
                                        items(media) { item ->
                                            if(isVideoUri(context, item)){
                                                MediaThumbnail(
                                                    attachment = Attachment.Video(item),
                                                    onClick = { },
                                                    isInSelectionMode = false,
                                                    isSelected = false
                                                )
                                            } else {
                                                MediaThumbnail(
                                                    attachment = Attachment.Image(item),
                                                    onClick = { },
                                                    isInSelectionMode = false,
                                                    isSelected = false
                                                )
                                            }

                                        }
                                    }*/
                                    MediaGridScreen(
                                        mediaItems = attachments,
                                        onMediaClick = { att ->
                                            select(attachments.indexOf(att))
                                            openMediaDetail = true
                                        },
                                        isInSelectionMode = false,
                                        onBack = { showAction = false }
                                    )
                                }
                                else {
                                    MediaDetailScreen(
                                        onBack = {
                                            openMediaDetail = false
                                            coroutinescope.launch {
                                                delay(500)
                                                select(-1)
                                            }
                                        },
                                        onNext = next,
                                        onPrevious = prev,
                                        allMedia = attachments,
                                        currentIndex = selectedIndex
                                    )
                                }
                            }
                        }
                        ContactActions.TX -> {
                            TransactionLogScreen(
                                scrollState,
                                transactions
                            )
                        }
                    }
            }
        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(dgenBlack)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            IconButton(onClick = {
                if(showAction){
                    showAction = false
                } else {
                    onBackClick()
                }

            }) {
                Icon(
                    painter = painterResource(R.drawable.backicon),
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(min = 10.dp, max = 250.dp)
                ,
            )
            IconButton(modifier = Modifier.alpha(0f), onClick = {  }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "BackButton",
                    modifier = Modifier.size(24.dp),
                    tint = dgenTurqoise
                )
            }

        }


        //Confirmation Overlay
        AnimatedVisibility(
            visible = showConfirmation,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
        ) {
            when(confirmation){
                ContactConfirmation.DELETEGROUP -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showConfirmation = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(36.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text("Do you want to delete ${title}?",
                                    style =
                                        TextStyle(
                                            textAlign = TextAlign.Center,
                                            fontFamily = PitagonsSans,
                                            color = dgenTurqoise,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = body1_fontSize,
                                            lineHeight = body1_fontSize,
                                            letterSpacing = 0.sp,
                                            textDecoration = TextDecoration.None,
                                        ),
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .fillMaxWidth()
                                )
                                Text("DELETE GROUP",
                                    modifier = Modifier.pointerInput(Unit){
                                        detectTapGestures {
                                            showConfirmation = false
                                            //TODO: Delete
                                            deleteGroup()
                                        }
                                    },
                                    style = TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontFamily = SpaceMono,
                                        color = dgenRed,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp,
                                        lineHeight = 20.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                    )
                                )
                            }
                    }
                }
                ContactConfirmation.DELETEMEMEBER -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showConfirmation = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ){

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            //TODO: Delete Random Member
                            Text("Do you want to remove ${selectedMember?.contact?.name} from the group?",
                                style =
                                    TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontFamily = PitagonsSans,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = body1_fontSize,
                                        lineHeight = body1_fontSize,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                    ),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                            )
                            Text("REMOVE ${selectedMember?.contact?.name}",
                                modifier = Modifier.pointerInput(Unit){
                                    detectTapGestures {
                                        showConfirmation = false
                                        deleteMember(selectedMember)
                                    }
                                },
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontFamily = SpaceMono,
                                    color = dgenRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None,
                                )
                            )
                        }
                    }
                }
                ContactConfirmation.LEAVEGROUP -> {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showConfirmation = false

                                }
                            },
                        contentAlignment = Alignment.Center
                    ){

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            //TODO: add leave group
                            Text("Do you want to leave ${title}?",
                                style =
                                    TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontFamily = PitagonsSans,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = body1_fontSize,
                                        lineHeight = body1_fontSize,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                    ),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                            )
                            Text("LEAVE GROUP",
                                modifier = Modifier.pointerInput(Unit){
                                    detectTapGestures {
                                        showConfirmation = false
                                        leaveGroup()
                                    }
                                },
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontFamily = SpaceMono,
                                    color = dgenRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None,
                                )
                            )
                        }
                    }
                }
                ContactConfirmation.DELETECONTACT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showConfirmation = false

                                }
                            },
                        contentAlignment = Alignment.Center
                    ){

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(36.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            //TODO: add leave group
                            Text("Do you want to delete ${title} as a contact?",
                                style =
                                    TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontFamily = PitagonsSans,
                                        color = dgenTurqoise,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = body1_fontSize,
                                        lineHeight = body1_fontSize,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                    ),
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                            )
                            Text("DELETE CONTACT",
                                modifier = Modifier.pointerInput(Unit){
                                    detectTapGestures {
                                        showConfirmation = false
                                        deleteContact()
                                    }
                                },
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    fontFamily = SpaceMono,
                                    color = dgenRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None,
                                )
                            )
                        }
                    }
                }
            }
        }


    }

}

enum class ContactActions {
    MEDIA, TX
}

enum class ContactConfirmation {
    DELETEGROUP, DELETEMEMEBER, LEAVEGROUP, DELETECONTACT
}

@Composable
fun ContactSection(
    title: String,
    amount: Int,
    onDone: () -> Unit,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures {
                    if (amount > 0) {
                        onDone()
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
        ) {
            Text(
                text =  buildAnnotatedString {
                    append(title)
                    append(" ")
                    withStyle(style = SpanStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise.copy(1f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append("$amount")
                    }
                },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if(amount > 0){
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Outlined.ChevronRight,
                tint = dgenTurqoise,
                contentDescription = "collapse"
            )
        }


    }

}

@Composable
fun MemberItem(
    title: String,
    onClick: () -> Unit,
    editMode: Boolean,
    isAdmin: Boolean
){
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append(title)
                if (isAdmin) {
                    withStyle(style = SpanStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise.copy(0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                    )
                    ) {
                        append("  ADMIN")
                    }
                }
            },
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenTurqoise,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        AnimatedVisibility(
            visible = editMode,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
        ) {
            if (!isAdmin) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onClick()
                            }
                        },
                    imageVector = Icons.Outlined.Delete,
                    tint = dgenRed,
                    contentDescription = "collapse"
                )
            }
        }
    }
}





@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun OverlayContactScreenPreview(){
    OverlayContactScreen(
        onBackClick = { },
        recipientUiState = RecipientUiState.Success(emptyList())
    )
}