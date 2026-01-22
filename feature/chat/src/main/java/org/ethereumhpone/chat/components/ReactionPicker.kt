package org.ethereumhpone.chat.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.dgenlibrary.theme.oceanAbyss
import org.ethereumphone.dgenlibrary.theme.oceanCore
import org.ethosmobile.components.library.theme.Colors

private const val TAG = "REACTION_DEBUG"

/**
 * A horizontal reaction picker that appears above messages on long press.
 * Shows quick emoji reactions with an option to expand for more.
 */
@Composable
fun ReactionPicker(
    isVisible: Boolean,
    isUserMe: Boolean,
    onReactionSelected: (String) -> Unit,
    onExpandPicker: () -> Unit = { Log.d(TAG, "ReactionPicker: DEFAULT EMPTY onExpandPicker called!") },
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,

    ) {
    // Log when ReactionPicker visibility changes
    LaunchedEffect(isVisible) {
        Log.d(TAG, "ReactionPicker: isVisible changed to $isVisible")
    }
    
    // Log callback identity
    Log.d(TAG, "ReactionPicker: COMPOSING with onExpandPicker hashCode=${onExpandPicker.hashCode()}")
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + 
                scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + 
               scaleOut(targetScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(secondaryColor)
                .border(1.dp, primaryColor, RoundedCornerShape(3.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            CommonReactions.quickReactions.forEach { emoji ->
                ReactionButton(
                    emoji = emoji,
                    onClick = { onReactionSelected(emoji) }
                )
            }
            
            // Expand button for more reactions
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(primaryColor)
                    .border(1.dp, primaryColor, RoundedCornerShape(3.dp))
                    .clickable { 
                        Log.d(TAG, "ReactionPicker: PLUS BUTTON CLICKED - calling onExpandPicker()")
                        onExpandPicker() 
                        Log.d(TAG, "ReactionPicker: PLUS BUTTON - onExpandPicker() called")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "More reactions",
                    tint = secondaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * A single emoji button in the reaction picker with press animation.
 */
@Composable
fun ReactionButton(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.3f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "reaction_scale"
    )
    
    Box(
        modifier = modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Expanded reaction picker showing all available reactions in a scrollable grid.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedReactionPicker(
    isVisible: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
) {
    // Log when ExpandedReactionPicker visibility changes
    LaunchedEffect(isVisible) {
        Log.d(TAG, "ExpandedReactionPicker: isVisible changed to $isVisible")
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)  // Wider to fit 6 emojis per row
                .heightIn(max = 300.dp) // Allow vertical scrolling
                .clip(RoundedCornerShape(3.dp))
                .background(secondaryColor)
                .border(1.dp, primaryColor, RoundedCornerShape(3.dp))

                // Consume clicks so they don't propagate to parent
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* consume click */ }
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Use FlowRow for a flexible grid that wraps to next line
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = 6
            ) {
                CommonReactions.allReactions.forEach { emoji ->
                    ReactionButton(
                        emoji = emoji,
                        onClick = { 
                            onReactionSelected(emoji)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun ReactionPickerPreview() {
    ReactionPicker(
        isVisible = true,
        isUserMe = false,
        onReactionSelected = {},
        onExpandPicker = {},
        primaryColor = oceanCore,
        secondaryColor = oceanAbyss
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun ExpandedReactionPickerPreview() {
    ExpandedReactionPicker(
        isVisible = true,
        onReactionSelected = {},
        onDismiss = {},
        primaryColor = oceanCore,
        secondaryColor = oceanAbyss

    )
}

