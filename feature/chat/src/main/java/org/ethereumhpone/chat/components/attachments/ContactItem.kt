package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ethereumhpone.domain.model.Attachment

@Composable
fun ContactItem(
    contact: Attachment.Contact,
    onClicked: (Attachment) -> Unit
) {
    Row(modifier = Modifier
        .clickable { onClicked(contact) }
    ) {
        // Icon

        Column {
            // contact name



            // see details
        }
    }

}