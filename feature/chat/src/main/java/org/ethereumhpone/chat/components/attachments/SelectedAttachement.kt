package org.ethereumhpone.chat.components.attachments

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.bouncycastle.math.raw.Mod

@Composable
fun SelectedAttachment(
    iconClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
        content()

        Box(Modifier.align(Alignment.TopEnd)) {
            Icon(
                imageVector = Icons.Outlined.Cancel,
                contentDescription = "",
                modifier = Modifier
                    .offset((4).dp, (-4).dp)
                    .clickable { iconClicked() }
                    .background(Color.White, CircleShape)
            )
        }
    }
}