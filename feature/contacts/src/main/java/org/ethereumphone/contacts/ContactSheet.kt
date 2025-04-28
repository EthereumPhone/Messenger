package org.ethereumphone.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.database.model.ContactEntity
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@Composable
fun ContactSheet(
    onDismiss: () -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val queryResultUiState by viewModel.queryResultUiState.collectAsStateWithLifecycle()

    ConversationSheet(
        queryResultUiState = queryResultUiState,
        onContactsSelected,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ConversationSheet(
    queryResultUiState: QueryResultUiState,
    onContactsSelected: (List<ContactEntity>) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
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


    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    )
                )
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 64.dp) // fab size 64.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Conversation".uppercase(),
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

            OldSchoolThickCursorTextField(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
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
                            text = "Search name or number",
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
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                ),
                cursorColor = dgenWhite,
            )

            LazyColumn {
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
                                        modifier = Modifier.fillMaxWidth(),
                                        text = "MAKE NEW GROUP",
                                        fontSize = 20.sp,
                                        style = TextStyle(
                                            fontFamily = PitagonsSans,
                                            color = dgenTurqoise.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        )
                                    )

                            }
                        }

                        queryResultUiState.manualContactEntity?.let {
                            item {
                                ethOSContactListItem(
                                    header = "write to ${it.lookupKey}"
                                )
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
                                        fontSize = 20.sp,
                                        style = TextStyle(
                                            fontFamily = PitagonsSans,
                                            color = dgenTurqoise.copy(alpha = 0.45f),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp
                                        )
                                    )
                                }
                            }
                        } else {
                            items(queryResultUiState.contactEntities) { contact ->
                                ethOSContactListItem(
                                    isMultiSelectMode = multiSelectMode,
                                    isSelected = selectedItems.contains(contact),
                                    header = contact.name,
                                    withSubheader = true,// ens in future ?
                                    subheader = contact.numbers.firstOrNull()?.address ?: "",
                                    onClick = {
                                        if(multiSelectMode) {
                                            if (contact in selectedItems) selectedItems.remove(contact) else selectedItems.add(contact)
                                        } else {
                                            onContactsSelected(listOf(contact))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { multiSelectMode = !multiSelectMode },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            val icon = if (!multiSelectMode) Icons.Default.Group else Icons.Default.GroupOff
            Icon(icon, "")
        }

        if (selectedItems.size != 0) {
            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                ,
                onClick = { onContactsSelected(selectedItems) }
            ) {
                Text("Create group")
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
            .padding(vertical = 6.dp)
    ){
        Column {
            Text(
                text = header,
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
                    .size(24.dp)
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
internal fun GroupSheet(
    members: List<ContactEntity>,
    onBackClick: () -> Unit,
    onCreateGroup: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit

) {

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    
    val buttonAlpha by animateFloatAsState(
        if (textState.text.isEmpty()) 0.35f else 1f,
        tween(300)
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp
                    )
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // fab size 64.dp
        ) {
            Row(
                modifier = Modifier
                    .background(dgenBlack)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(org.ethereumphone.dgenlibrary.R.drawable.backicon),
                        contentDescription = "BackButton",
                        modifier = Modifier.size(24.dp),
                        tint = dgenTurqoise
                    )
                }
                Text(
                    text = "NEW GROUP",
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
                IconButton(onClick = {  }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "BackButton",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Transparent
                    )
                }

            }
            Row(
                Modifier.padding(horizontal = 8.dp)
            ){
                OldSchoolThickCursorTextField(
                    modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.padding(horizontal = 8.dp).height(275.dp),
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
                Box {
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

        Surface(
            color = dgenTurqoise,
            shape = CircleShape,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .align(Alignment.BottomCenter)
                .alpha(buttonAlpha)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onCreateGroup()
                    }
                },
        ){
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CREATE GROUP",
                    color = dgenOcean,
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    modifier = Modifier
                )
            }

        }
    }
}


@Composable
fun ethOSContactListItem(
    header: String = "Header",
    withSubheader: Boolean = false,
    subheader: String = "Subheader",
    backgroundColor: Color = Colors.TRANSPARENT,
    colorOnBackground: Color = Colors.WHITE,
    subheaderColorOnBackground: Color = Colors.WHITE,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {

        ListItem(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            headlineContent = {
                Text(
                    text = header,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            },
            supportingContent = {


            },
            trailingContent = {
                if (isMultiSelectMode) {

                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = dgenTurqoise,
                            uncheckedColor = dgenGreen,
                            checkmarkColor = dgenOcean
                        ),
                    )
                }
            },


            colors = ListItemDefaults.colors(
                headlineColor = colorOnBackground,
                supportingColor = subheaderColorOnBackground,
                containerColor = backgroundColor
            )
        )
}


@Preview
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
        {}
    )
}

@Preview
@Composable
fun previewNoQueryContactSheet() {

    val contactEntities = listOf(
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
        ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),ContactEntity(name = "Nicola"),
        ContactEntity(name = "Also Nicola"),
        ContactEntity(name = "Mar... Sike, Nicola again"),
    )

    val queryResultUiState = QueryResultUiState.Success(null, contactEntities)

    ConversationSheet(
        queryResultUiState,
        {},
        {}
    )
}

@Preview
@Composable
fun previewNoContactsContactSheet() {

    val contactEntities = emptyList<ContactEntity>()

    val queryResultUiState = QueryResultUiState.Success(ContactEntity(name = "Nicola"), contactEntities)

    ConversationSheet(
        queryResultUiState,
        {},
        {}
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

    GroupSheet(
        contactEntities,
        {},
        {},
        {it ->},
        {it ->}

    )
}