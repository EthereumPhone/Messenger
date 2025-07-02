package org.ethereumhpone.contracts.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.lazerCore


@Composable
fun ConversationActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color = Color.Transparent,
    contentDescription: String = "ConversationActionButton",
    icon: ImageVector,
    iconColor: Color = lazerCore,
    iconSize: Dp = 32.dp
){
    IconButton(
        modifier = modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(0.dp)),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            backgroundColor
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(iconSize)
        )
    }
}
