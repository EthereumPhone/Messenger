package org.ethereumhpone.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import kotlin.math.roundToInt

// Custom shape for the tooltip with a pointer at the top
@Composable
fun TooltipShape(pointerSize: Dp = 10.dp, cornerRadius: Dp = 8.dp): Shape {

    val density = LocalDensity.current
    return GenericShape { size, _ ->
        val width = size.width
        val height = size.height
        val pointerSizePx = with(density) { pointerSize.toPx() }
        val cornerRadiusPx = height / 2f // Use half of the height to create pill shape ends

        // Draw the pill-shaped rectangle with pointer
        val path = Path().apply {
            // Start at the top left corner
            moveTo(cornerRadiusPx, 0f)
            // Top-left corner arc
            arcTo(
                rect = Rect(
                    0f, 0f, cornerRadiusPx * 2, height.toFloat()
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Right side
            lineTo(width - cornerRadiusPx, 0f)
            // Top-right corner arc
            arcTo(
                rect = Rect(
                    width - cornerRadiusPx * 2, 0f, width.toFloat(), height.toFloat()
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Draw the pointer at the center bottom
            lineTo(width / 2 + pointerSizePx, height.toFloat())
            lineTo(width / 2, height + pointerSizePx)
            lineTo(width / 2 - pointerSizePx, height.toFloat())
            close()
        }
        addPath(path)
    }
}

@Composable
fun TooltipButtonWithCustomShape() {
    var showTooltip by remember { mutableStateOf(false) }
    var buttonPosition by remember { mutableStateOf(IntOffset.Zero) }
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Button with tooltip
        Button(
            onClick = { showTooltip = !showTooltip },
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    // Capture the button's position and size to calculate the Popup position
                    buttonPosition = coordinates.positionInWindow().run { IntOffset(x.roundToInt(), y.roundToInt()) }
                    buttonSize = coordinates.size.toSize().run { IntSize(width.roundToInt(), height.roundToInt()) }
                }
        ) {
            Text("Click me!")
        }

        // Tooltip with upward translation and fade-in animation
        if (showTooltip) {
            Popup(
                offset = IntOffset(
                    x = buttonPosition.x + (buttonSize.width / 2) - 150, // Center the popup relative to the button
                    y = buttonPosition.y - buttonSize.height
                ),
                alignment = Alignment.TopStart
            ) {
                AnimatedVisibility(
                    visible = showTooltip,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }), // Slide upward and fade in
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })  // Slide downward and fade out
                ) {
                    TooltipContentWithCustomShape { showTooltip = false }
                }
            }
        }
    }
}

@Composable
fun TooltipContentWithCustomShape(onDismiss: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 300.dp, height = 100.dp), // Set a fixed width and height to center properly
        shape = TooltipShape() // Apply the custom tooltip shape here
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
        ) {
            BasicText(text = "This tooltip has a toggle!")
            Spacer(modifier = Modifier.height(8.dp))

            // Toggle (Switch) inside the tooltip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isChecked) "Enabled" else "Disabled")
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onDismiss() }) {
                Text("Dismiss")
            }
        }
    }
}

@Preview
@Composable
fun TooltipButtonWithCustomShapePreview() {
    //TooltipButtonWithCustomShape()
    var list = listOf("SMS","XMTP")
    var index = remember {
        mutableStateOf(0)
    }
    CustomTabSelector(index,list,{ index.value = it })
}

@Composable
fun CustomTabSelector(
    selectedTabIndex: MutableState<Int>,
    tabTitles: List<String>,       // Currently selected tab index
    onTabSelected: (Int) -> Unit,          // Callback for when a tab is selected
    selectedTabColor: Color = Colors.WHITE,  // Color of the selected tab
    unselectedTabColor: Color = Colors.TRANSPARENT // Color of the unselected tabs
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(Colors.TRANSPARENT, shape = CircleShape)
    ) {
        tabTitles.forEachIndexed { index, title ->
            val isSelected = index == selectedTabIndex.value
            TabItem(
                title = title,
                isSelected = isSelected,
                onTabClick = { onTabSelected(index) },
                selectedTabColor = selectedTabColor,
                unselectedTabColor = unselectedTabColor
            )
        }
    }
}

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    onTabClick: () -> Unit,
    selectedTabColor: Color,
    unselectedTabColor: Color
) {
    val backgroundColor = if (isSelected) selectedTabColor else unselectedTabColor
    val textColor = if (isSelected) Colors.BLACK else Colors.WHITE
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { onTabClick() },
                    onTap = { onTabClick() },
                )
            }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontFamily = Fonts.INTER,
            fontWeight = fontWeight
        )
    }
}
