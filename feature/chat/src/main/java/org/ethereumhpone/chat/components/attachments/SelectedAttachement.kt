package org.ethereumhpone.chat.components.attachments

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SelectedAttachment(
    iconClicked: () -> Unit,
    content: @Composable () -> Unit
) {

    var animateAttachment by remember {
        mutableStateOf(false)
    }
    val attachmentAnimation by animateFloatAsState(

        // specifying target value on below line.
        targetValue = if (animateAttachment) 1f else 0f,

        // on below line we are specifying
        // animation specific duration's 1 sec
        animationSpec = tween(1000,)
    )

    val animatedSize by animateDpAsState(
        targetValue = if (animateAttachment) 100.dp else 0.dp,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(true) {
        animateAttachment = true

    }
        Box(
            modifier = Modifier.size(animatedSize).alpha(attachmentAnimation)
        ) {
            content()

            Box(Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = "",
                    modifier = Modifier
                        .offset((10).dp, (-10).dp)
                        .clickable { iconClicked() }
                        .background(Color.White, CircleShape)
                )
            }
        }


}