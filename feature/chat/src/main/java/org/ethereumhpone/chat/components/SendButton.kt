package org.ethereumhpone.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import org.ethosmobile.components.library.theme.Colors
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SendButton(
    background: Color,
    list: List<String>,
    selectedTab: MutableState<Int>,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var showTooltip by remember { mutableStateOf(false) }

    var buttonPosition by remember { mutableStateOf(IntOffset.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }


        Column( horizontalAlignment = Alignment.End, modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap={
                    showTooltip = !showTooltip
                },
                onPress={
                    showTooltip = !showTooltip
                },
                onTap={
                    showTooltip = !showTooltip
                },
                onLongPress = {
                    showTooltip = !showTooltip
                }
            )
        }) {

            AnimatedVisibility(
                visible = showTooltip,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }), // Slide upward and fade in
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),  // Slide downward and fade out
            ) {
                Popup(
                    offset = IntOffset(
                        x = 0,//-(buttonSize.width/2) , // Center the popup relative to the button
                        y = -(buttonSize.height+100)
                    ),
                    alignment = Alignment.TopCenter
                ) {
                    TooltipContent(
                        selectedTab = selectedTab, list = list, onDismiss = { showTooltip = false }
                    )
                }
            }

            val interactionSource = remember { MutableInteractionSource() }

            //val isDragged by interactionSource.col()
            val isPressed by interactionSource.collectIsPressedAsState()

// Use the state to change our UI
//            val (text, color) =
//                when {
//                    isDragged && isPressed -> "Dragged and pressed" to Color.Red
//                    isDragged -> "Dragged" to Color.Green
//                    isPressed -> "Pressed" to Color.Blue
//                    // Default / baseline state
//                    else -> "Drag me horizontally, or press me!" to Color.Black
//                }
            Box(
                contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(CircleShape)
                        .background(background)
                        .size(42.dp)
                        .onGloballyPositioned { coordinates ->
                            // Capture the button's position and size to calculate the Popup position
                            buttonPosition = coordinates
                                .positionInWindow()
                                .run { IntOffset(x.roundToInt(), y.roundToInt()) }
                            buttonSize = coordinates.size
                                .toSize()
                                .run { IntSize(width.roundToInt(), height.roundToInt()) }
                        }
                        .combinedClickable(
                            onClick = {  },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                showTooltip = true
                            },
                            //onLongClickLabel = stringResource(R.string.open_context_menu)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        modifier = Modifier
                            .size(32.dp),
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }



            // Button with tooltip
//            Button(
//                onClick = { showTooltip = !showTooltip },
//                modifier = Modifier
//                    .onGloballyPositioned { coordinates ->
//                        // Capture the button's position and size to calculate the Popup position
//                        buttonPosition = coordinates.positionInWindow().run { IntOffset(x.roundToInt(), y.roundToInt()) }
//                        buttonSize = coordinates.size.toSize().run { IntSize(width.roundToInt(), height.roundToInt()) }
//                    }
//            ) {
//                Text("Hover or Click me!")
//            }

            // Tooltip Popup








}

@Composable
fun TooltipContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    list: List<String>,
    selectedTab: MutableState<Int>
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = Colors.BLACK,
            contentColor= Colors.WHITE,
        ),
    elevation = cardElevation(8.dp),
    border = BorderStroke(2.dp, Colors.WHITE),

    modifier = modifier
        .width(IntrinsicSize.Min)
        .height(IntrinsicSize.Min)
             // Set a fixed width to center properly
    ) {
        Box(contentAlignment =  Alignment.Center, modifier = Modifier
            .background(Colors.BLACK)
            .padding(8.dp)
            .height(IntrinsicSize.Max)
            .width(IntrinsicSize.Max)){
            CustomTabSelector(selectedTab,list,{
                selectedTab.value = it
                onDismiss()
            })
        }

    }

}

@Preview
@Composable
fun SendButtonPreview() {
    //SendButton()
}
