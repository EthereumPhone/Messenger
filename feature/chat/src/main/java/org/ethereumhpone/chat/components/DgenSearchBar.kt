package org.ethereumhpone.chat.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethosmobile.contacts.ui.components.DgenCursorSearchTextfield
import org.ethereumphone.dgenlibrary.R
import kotlinx.coroutines.delay
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenWhite

@Composable
fun DgenSearchBar(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    focusedSearch: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    textColor: Color,
    backgroundColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    componentBackgroundColor: Color = dgenBlack.copy(alpha = 0.9f),
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onClear: () -> Unit,
    onNavigateBack: () -> Unit = {},
    leadingIconResId: Int = R.drawable.searchicon,
    selectedChainId: Int? = null,
    onNetworkClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showChainButton: Boolean = true
) {
    var internalTfv by remember {
        mutableStateOf(TextFieldValue(text = searchValue, selection = TextRange(searchValue.length)))
    }

    LaunchedEffect(searchValue) {
        if (internalTfv.text != searchValue) {
            internalTfv = TextFieldValue(text = searchValue, selection = TextRange(searchValue.length))
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = componentBackgroundColor,
                    size = size
                )
            }
        
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
                .height(48.dp)
                .drawBehind {
                    drawRoundRect(
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                        color = backgroundColor,
                        alpha = 0.7f
                    )
                }
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = modifier
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(leadingIconResId),
                    contentDescription = "Search",
                    tint = primaryColor,
                    modifier = modifier.size(24.dp)
                )
            }

            DgenCursorSearchTextfield(
                value = internalTfv,
                onValueChange = { newTextFieldValue ->
                    val processedText = newTextFieldValue.text.replace(Regex("\\.\\s+"), ".")
                    internalTfv = newTextFieldValue.copy(text = processedText)
                    if (searchValue != processedText) {
                        onSearchValueChange(processedText)
                    }
                },
                modifier = modifier
                    .padding(end = 8.dp)
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                maxFieldHeight = 50.dp,
                cursorColor = primaryColor,
                cursorWidth = 14.dp,
                cursorHeight = 24.dp,
                onFocusChanged = { onFocusChanged(it) },
                placeholder = {
                    Text(
                        modifier = modifier.fillMaxWidth().offset(y = 2.dp),
                        text = "SEARCH",
                        style = TextStyle(
                            textAlign = TextAlign.Start,
                            fontFamily = SpaceMono,
                            color = textColor.copy(pulseOpacity),
                            fontSize = label_fontSize,
                            lineHeight = label_fontSize,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    )
                },
                textStyle = TextStyle(
                    textAlign = TextAlign.Start,
                    fontFamily = SpaceMono,
                    color = dgenWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = body1_fontSize,
                    lineHeight = body1_fontSize,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                )
            )

            Crossfade(
                targetState = searchValue.isNotEmpty(),
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                ),
                label = "search_action_crossfade"
            ) { hasText ->
                Row(
                    modifier = Modifier.then(
                        if (searchValue.isNotEmpty()) {
                            Modifier.width(32.dp)
                        } else {
                            Modifier.width(80.dp)
                        }
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (hasText) {
                        Box(
                            modifier = modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(50))
                                .background(textColor)
                                .clickable(onClick = onClear),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                contentDescription = "Clear",
                                imageVector = Icons.Rounded.Clear,
                                tint = backgroundColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else if (showChainButton) {
                        ChainButton(
                            chainId = selectedChainId,
                            isSelected = false,
                            primaryColor = primaryColor,
                            onClick = onNetworkClick,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChainButton(
    chainId: Int? = null,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) primaryColor else Color.Transparent)
            .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChainIcon(chainId = chainId, size = 16.dp)
            Text(
                text = getChainName(chainId),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) dgenBlack else primaryColor
                )
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select chain",
                tint = if (isSelected) dgenBlack else primaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
