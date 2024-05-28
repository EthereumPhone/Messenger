package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SelectedAttachment(
    iconClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    Box {
        Icon(
            imageVector = Icons.Outlined.Cancel,
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { iconClicked() }
        )
        content()
    }
}


@Composable
@Preview
fun PreviewSelectableAttachment() {
}