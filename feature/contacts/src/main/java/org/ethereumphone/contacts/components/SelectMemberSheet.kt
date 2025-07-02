package org.ethereumphone.contacts.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumhpone.chat.components.InputSelector
import org.ethereumhpone.chat.components.OldSchoolThickCursorTextField
import org.ethereumhpone.database.model.ContactEntity
import org.ethereumphone.contacts.QueryResultUiState
import org.ethereumphone.contacts.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectMembersSheet(
    queryResultUiState: QueryResultUiState,
    onSearchQueryChanged: (String) -> Unit,
    onContactsSelected: (List<ContactEntity>) -> Unit,
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
                        .padding(start = 12.dp, end = 12.dp, bottom = 24.dp) // fab size 64.dp
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
                                    SelectableMemberItem(
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
                CreateGroupSheet(
                    members = selectedItems,
                    onBackClick = { showFinalGroupSheet = false },
                    onCreateGroup = { members ->
                        showFinalGroupSheet = false
                        onContactsSelected(members)
                    },
                )
            }


        }


    }
}