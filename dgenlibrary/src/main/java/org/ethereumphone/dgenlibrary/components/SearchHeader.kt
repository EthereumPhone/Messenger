package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.example.dgenlibrary.ui.theme.mediumExitDuration
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield


@Composable
fun SearchHeader(
    searchValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClearValue: () -> Unit,
    focusManager: FocusManager,
    backgroundColor: Color,
    onAddContact: () -> Unit,
    isSearchFocused: MutableState<Boolean>,
    focusRequester: FocusRequester,
    primaryColor: Color,
    secondaryColor: Color,
    keyboardController: SoftwareKeyboardController?
){
    val animatedColor by animateColorAsState(
        targetValue = if (isSearchFocused.value) secondaryColor else Color.Transparent,
        animationSpec = tween(durationMillis = mediumEnterDuration),
        label = "color"
    )

    val animatedPlaceholderColor by animateColorAsState(
        targetValue = if (isSearchFocused.value) primaryColor.copy(pulseOpacity) else primaryColor,
        animationSpec = tween(durationMillis = mediumEnterDuration),
        label = "color"
    )

    LaunchedEffect(isSearchFocused.value) {
        if (isSearchFocused.value) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(start = 12.dp, end = 12.dp, top = 16.dp ,bottom = 8.dp)
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            color = animatedColor,
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        modifier = Modifier,
                        targetState = isSearchFocused.value,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(mediumEnterDuration)) togetherWith
                                    fadeOut(animationSpec = tween(mediumExitDuration))
                        }
                    ) { state ->
                        if (state){
                            Icon(
                                painter = painterResource(R.drawable.backicon),
                                contentDescription = "Back",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .pointerInput(true) {
                                        detectTapGestures {
                                            isSearchFocused.value = false
                                        }
                                    }
                            )
                        }
                        else {
                            Icon(
                                painter = painterResource(R.drawable.searchicon),
                                contentDescription = "Search",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            isSearchFocused.value = true
                                        }
                                    }
                            )
                        }
                    }
                }

                DgenCursorSearchTextfield(
                    value = searchValue,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        textAlign = TextAlign.Start,
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = body1_fontSize,
                        lineHeight = 36.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    onFocusChanged = { focused ->
                        if (isSearchFocused.value != focused) {
                            isSearchFocused.value = focused
                        }
                    },
                    singleLine = true,
                    cursorColor = primaryColor,
                    cursorWidth = 16.dp,
                    textfieldFocusManager = focusManager,
                    placeholder = {
                        Text(
                            text = "SEARCH",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = animatedPlaceholderColor,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        modifier = Modifier,
                        targetState = isSearchFocused.value,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(mediumEnterDuration)) togetherWith
                                    fadeOut(animationSpec = tween(mediumExitDuration))
                        }
                    ) { state ->
                        if (state){
                            ActionButton(
                                modifier = Modifier
                                    .size(24.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = primaryColor,
                                        )
                                    },
                                onClick = onClearValue,
                                icon = {
                                    Icon(
                                        contentDescription = "Clear",
                                        imageVector = Icons.Rounded.Clear,
                                        tint = secondaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Contact",
                                tint = primaryColor,
                                modifier = Modifier
                                    .size(36.dp)
                                    .pointerInput(true) {
                                        detectTapGestures {
                                            onAddContact()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Brush.verticalGradient(listOf(dgenBlack, Color.Transparent)))
        )
    }
}