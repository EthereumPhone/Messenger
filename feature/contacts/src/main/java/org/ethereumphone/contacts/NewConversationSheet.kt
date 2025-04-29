package org.ethereumphone.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.components.CheckBox


@Composable
fun NewConversationSheet(
    onDismiss: () -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val queryResultUiState by viewModel.queryResultUiState.collectAsStateWithLifecycle()

    var openGroupCreation by remember { mutableStateOf(false) }


    ConversationSheet(
        queryResultUiState = queryResultUiState,
        onContactsSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onOpenGroupClick = {
            openGroupCreation = true
        },
        onDismiss = onDismiss
    )



}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationSheet(
    queryResultUiState: QueryResultUiState,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onOpenGroupClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    var multiSelectMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }


    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }
    var textFieldFocusState by remember { mutableStateOf(false) }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {
        AnimatedContent(
            multiSelectMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) togetherWith
                        fadeOut(animationSpec = tween(150))
            }
        ) { open ->
            if(!open){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier
                        .fillMaxSize()

                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp) // fab size 64.dp
                ) {
                    Row(
                        modifier = Modifier

                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ){

                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                                contentDescription = "BackButton",
                                modifier = Modifier.size(24.dp),
                                tint = dgenTurqoise
                            )
                        }

                        Text(
                            text = "NEW CONVERSATION",
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
                            modifier = Modifier,
                        )

                        IconButton(modifier = Modifier.alpha(0f), onClick = {  }) {
                            Icon(
                                painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                                contentDescription = "BackButton",
                                modifier = Modifier.size(24.dp),
                                tint = dgenTurqoise
                            )
                        }

                    }

                    Row(
                        Modifier.padding(horizontal = 16.dp)
                    ){
                        OldSchoolThickCursorTextField(
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = textState,
                            onValueChange = { newTextFieldValue ->
                                // Process the text to remove spaces after periods
                                val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")

                                // Create a new TextFieldValue with the processed text and updated selection
                                val newProcessedTextFieldValue = newTextFieldValue.copy(text = processedText)
                                textState = newProcessedTextFieldValue
                                onSearchQueryChanged(newProcessedTextFieldValue.text)
                            },
                            placeholder = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.searchicon),
                                        contentDescription = "Searching",
                                        tint = dgenTurqoise.copy(alpha = 0.45f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Search name or phonenumber".uppercase(),
                                        style = TextStyle(
                                            fontFamily = SpaceMono,
                                            color = dgenTurqoise.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        )
                                    )
                                }

                            },
                            textStyle = TextStyle(
                                fontFamily = PitagonsSans,
                                color = dgenWhite,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            ),
                            cursorColor = dgenWhite,
                        )
                    }



                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when(queryResultUiState) {
                            is QueryResultUiState.Loading -> {}
                            is QueryResultUiState.Success -> {

                                stickyHeader {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(dgenBlack)
                                            .padding(vertical = 8.dp)
                                    ) {

                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(Unit) {
                                                    detectTapGestures {
                                                        multiSelectMode = true
                                                    }
                                                },
                                            text = "MAKE NEW GROUP",
                                            fontSize = 24.sp,
                                            style = TextStyle(
                                                fontFamily = PitagonsSans,
                                                color = dgenTurqoise,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp
                                            )
                                        )

                                    }
                                }

                                if(textState.text.isNotEmpty()){
                                    queryResultUiState.manualContactEntity?.let {
                                        item {

                                            Text(
                                                modifier = Modifier.fillMaxWidth(),
                                                text = "write to ${it.lookupKey}",
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 22.sp,
                                                    lineHeight = 22.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                )
                                            )
                                        }
                                    }
                                }



                                if (queryResultUiState.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier
                                            .fillParentMaxHeight(0.5f)
                                            .fillParentMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No contacts available".uppercase(),
                                                fontSize = 24.sp,
                                                style = TextStyle(
                                                    fontFamily = SpaceMono,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 24.sp
                                                )
                                            )
                                        }
                                    }
                                } else {
                                    items(queryResultUiState.contactEntities) { contact ->
                                        Column {
                                            Text(
                                                text = contact.name,
                                                overflow = TextOverflow.Ellipsis,
                                                style = TextStyle(
                                                    fontFamily = PitagonsSans,
                                                    color = dgenTurqoise,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 22.sp,
                                                    lineHeight = 22.sp,
                                                    letterSpacing = 0.sp,
                                                    textDecoration = TextDecoration.None
                                                )
                                            )
                                            if(contact.numbers.firstOrNull()?.address != null){
                                                Text(
                                                    text = contact.numbers.firstOrNull()?.address!!,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenTurqoise.copy(0.45f),
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 16.sp,
                                                        lineHeight = 16.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
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
            else {
                SelectMembersSheet(
                    queryResultUiState = queryResultUiState,
                    onCreateGroup = { multiSelectMode = false },
                    onSearchQueryChanged = onSearchQueryChanged,
                    onBackClick = { multiSelectMode = false },
                )
            }
        }
    }

    // clear list if user quits multiselect
    LaunchedEffect(multiSelectMode) {
        if(!multiSelectMode) {
            selectedItems.clear()
        }
    }
}


@Composable
fun MemberItem(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    header: String,
    subheader: String = ""
){
    var openDelete by remember { mutableStateOf(false) }
    Row (
        modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        openDelete = !openDelete
                    },
                    onDoubleTap = {
                        openDelete = !openDelete
                    }
                )
            }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ){
        Column {
            Text(
                text = header,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
            if(subheader.isNotEmpty()){
                Text(
                    text = subheader,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise.copy(0.45f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        AnimatedVisibility(
            openDelete,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                Icons.Outlined.Delete,
                "Delete",
                tint = dgenRed,
                modifier = Modifier
                    .size(28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onDelete()
                        }
                    }
            )
        }
    }
}

@Composable
fun MemberSelectItem(
    modifier: Modifier = Modifier,
    header: String,
    subheader: String = "",
    isSelected: Boolean,
    onCheckClick: () -> Unit = {},
){
    var openDelete by remember { mutableStateOf(false) }
    Row (
        modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        openDelete = !openDelete
                    },
                    onDoubleTap = {
                        openDelete = !openDelete
                    }
                )
            }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Column {
            Text(
                text = header,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
            if(subheader.isNotEmpty()){
                Text(
                    text = subheader,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise.copy(0.45f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        CheckBox(
            checked = isSelected,
            onCheckedChange = { it -> onCheckClick() },

        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SelectMembersSheet(
    queryResultUiState: QueryResultUiState,
    onCreateGroup: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }


    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val buttonAlpha by animateFloatAsState(
        if (selectedItems.size > 0) 1f else 0f,
        tween(300)
    )
     var showFinalGroupSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {

            AnimatedContent(
                showFinalGroupSheet,
                transitionSpec = {
                    fadeIn(animationSpec = tween(150, 150)) togetherWith
                            fadeOut(animationSpec = tween(150))
                }
            ) { open ->
                if (!open){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp) // fab size 64.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .background(dgenBlack)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                                    contentDescription = "BackButton",
                                    modifier = Modifier.size(24.dp),
                                    tint = dgenTurqoise
                                )
                            }
                            Text(
                                text = "SELECT",
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
                                modifier = Modifier,
                            )

                            Surface(
                                color = dgenTurqoise,
                                shape = CircleShape,
                                modifier = Modifier
                                    .height(25.dp)
                                    .alpha(buttonAlpha)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            if (selectedItems.size > 0) {
                                                showFinalGroupSheet = true
                                            }
                                        }
                                    }
                            ){
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                    ,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text= "NEXT", color = dgenOcean , style = TextStyle(
                                        fontFamily = SpaceMono,
                                        color = dgenWhite,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                        lineHeight = 16.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None
                                    ),modifier = Modifier.padding(horizontal = 8.dp))
                                }

                            }
                        }

                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            OldSchoolThickCursorTextField(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = textState,
                                onValueChange = { newTextFieldValue ->
                                    // Process the text to remove spaces after periods
                                    val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")

                                    // Create a new TextFieldValue with the processed text and updated selection
                                    val newProcessedTextFieldValue = newTextFieldValue.copy(text = processedText)
                                    textState = newProcessedTextFieldValue
                                    onSearchQueryChanged(newProcessedTextFieldValue.text)
                                },
                                placeholder = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.searchicon),
                                            contentDescription = "Searching",
                                            tint = dgenTurqoise.copy(alpha = 0.45f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Search name or phonenumber".uppercase(),
                                            style = TextStyle(
                                                fontFamily = SpaceMono,
                                                color = dgenTurqoise.copy(alpha = 0.45f),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 20.sp
                                            )
                                        )
                                    }

                                },
                                textStyle = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenWhite,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    lineHeight = 20.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                ),
                                cursorColor = dgenWhite,
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when(queryResultUiState) {
                                is QueryResultUiState.Loading -> {}
                                is QueryResultUiState.Success -> {
                                    if(textState.text.isNotEmpty()){
                                        queryResultUiState.manualContactEntity?.let {
                                            item {
                                                Text(
                                                    text = "write to ${it.lookupKey}",
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = TextStyle(
                                                        fontFamily = PitagonsSans,
                                                        color = dgenTurqoise,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 22.sp,
                                                        lineHeight = 22.sp,
                                                        letterSpacing = 0.sp,
                                                        textDecoration = TextDecoration.None
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    items(queryResultUiState.contactEntities) { contact ->
                                        MemberSelectItem(
                                            isSelected = selectedItems.contains(contact),
                                            header = contact.name,
                                            subheader = contact.numbers.firstOrNull()?.address ?: "",
                                            onCheckClick = {
                                                if (contact in selectedItems) selectedItems.remove(contact) else selectedItems.add(contact)
                                            }
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
                else{
                    FinalGroupSheet(
                        members = selectedItems,
                        onBackClick = { showFinalGroupSheet = false },
                        onCreateGroup = {
                            showFinalGroupSheet = false
                            onCreateGroup()
                        },

                    )
                }


            }

        
    }
}

@Composable
internal fun FinalGroupSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: () -> Unit,
) {

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    
    val buttonAlpha by animateFloatAsState(
        if (textState.text.isEmpty()) 0.35f else 1f,
        tween(300)
    )



        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                        contentDescription = "BackButton",
                        modifier = Modifier.size(24.dp),
                        tint = dgenTurqoise
                    )
                }

                Surface(
                    color = dgenTurqoise,
                    shape = CircleShape,
                    modifier = Modifier
                        .height(25.dp)
                        .alpha(buttonAlpha)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onCreateGroup()
                            }
                        }
                ){
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp)
                        ,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text= "CREATE GROUP", color = dgenOcean , style = TextStyle(
                            fontFamily = SpaceMono,
                            color = dgenWhite,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),modifier = Modifier.padding(horizontal = 8.dp))
                    }

                }
                

            }
            Row(
                Modifier.padding(horizontal = 8.dp)
            ){
                OldSchoolThickCursorTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = textState,
                    onValueChange = { newTextFieldValue ->
                        textState = newTextFieldValue
                    },
                    placeholder = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = "Type group name",
                                style = TextStyle(
                                    fontFamily = PitagonsSans,
                                    color = dgenTurqoise.copy(alpha = 0.45f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                )
                            )
                        }

                    },
                    textStyle = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    cursorColor = dgenWhite,
                    singleLine = true
                )
            }


            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MEMBERS ${members.size}",
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
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(members) { contact ->
                            MemberItem(
                                header = contact.name,
                                subheader = contact.numbers.firstOrNull()?.address ?: "",
                                onDelete = {},
                            )
                        }
                    }
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.TopCenter)
                        .background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent))))
                    Spacer(modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .align(Alignment.BottomCenter)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, dgenBlack))))

                }

            }

        }

}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewContactSheet() {

    val contactEntities = listOf(
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again")
    )

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        queryResultUiState,
        {},
        {},
        onOpenGroupClick = { TODO() },{}
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewNoContactsContactSheet() {

    val contactEntities = emptyList<ContactEntity>()

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        queryResultUiState,
        {},
        {},{},{}
    )
}


@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun previewGroup() {

    val contactEntities = listOf(
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
    )

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    FinalGroupSheet(
        contactEntities,
        {},
        {},
//        {it ->}

    )
}