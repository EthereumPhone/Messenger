package org.ethereumhpone.chat.components.attachments

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Box(
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.DarkGray)
            .padding(5.dp)
    ) {
        Row(modifier = modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon

            if (contact.imageUri == null) {
                Image(
                    painter = painterResource(id = R.drawable.nouns_placeholder),
                    contentDescription = "contact Profile Pic",
                    modifier = Modifier
                        .clip(CircleShape)
                )
            } else {
                AsyncImage(
                    model = contact.imageUri, contentDescription = "",
                    placeholder = painterResource(id = R.drawable.nouns_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(CircleShape)
                        .aspectRatio(1f)
                )
            }

            Column {
                // contact name
                val data = Ezvcard.parse(contact.vCard).first()
                Text(
                    data.getDisplayName()?.trimEllipse(12) ?: "",
                    color = Color.White,
                    fontSize = 18.sp,
                    maxLines = 1
                )

                // see ens
                Text(
                    "Contact details",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
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

fun String.trimEllipse(size: Int): String {
    if (this.length > size) {
        return this.take(size)+"..."
    }

    return this
}

@Preview
@Composable
fun PreviewContactItem() {

    val uri = "android.resource://${LocalContext.current.packageName}/${R.drawable.ethos}"


    val vcard = "BEGIN:VCARD\r\n" +
            "VERSION:4.0\r\n" +
            "N:Cernea;Nicola;;CTO;\r\n" +
            "FN:Nicola Ceornea\r\n" +
            "DATA15: myEns\r\n\""
            "END:VCARD\r\n";

    ContactItem(
        modifier = Modifier,
        Attachment.Contact(
            imageUri = Uri.parse(uri),
            vCard = vcard)

    )
}