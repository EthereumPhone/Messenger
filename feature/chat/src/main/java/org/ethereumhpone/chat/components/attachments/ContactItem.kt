package org.ethereumhpone.chat.components.attachments

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ezvcard.Ezvcard
import ezvcard.VCard
import org.ethereumhpone.chat.R
import org.ethereumhpone.domain.model.Attachment

@Composable
fun ContactItem(
    modifier: Modifier = Modifier,
    contact: Attachment.Contact,
) {
    Row(modifier = modifier) {
        // Icon

        if (contact.imageUri == null) {
            Image(
                painter = painterResource(id = R.drawable.nouns_placeholder),
                contentDescription = "contact Profile Pic",
                modifier = Modifier.size(43.dp)
            )
        } else {
            AsyncImage(model = contact.imageUri, contentDescription = "")
        }

        Column {
            // contact name
            val data = Ezvcard.parse(contact.vCard).first()
            Text(data.getDisplayName() ?: "")

            // see ens
            Text(data.getEns() ?: "")

        }
    }
}

fun VCard.getDisplayName(): String? {
    return formattedName?.value
        ?: telephoneNumbers?.firstOrNull()?.text
        ?: emails.firstOrNull()?.value
}

fun VCard.getEns(): String? {
    return getExtendedProperty("DATA15")?.value
}

@Preview
@Composable
fun PreviewContactItem() {



    val vcard = "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "N:Doe;Jonathan;;Mr;\r\n" +
            "FN:John Doe\r\n" +
            "DATA15: myEns\r\n\""
            "END:VCARD\r\n";

    ContactItem(
        modifier = Modifier,
        Attachment.Contact(vCard = vcard)
    )
}