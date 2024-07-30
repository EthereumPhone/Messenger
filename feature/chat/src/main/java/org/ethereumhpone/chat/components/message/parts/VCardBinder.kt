package org.ethereumhpone.chat.components.message.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ezvcard.Ezvcard
import org.ethereumhpone.chat.components.attachments.getDisplayName
import org.ethereumhpone.database.model.Message
import org.ethereumhpone.database.model.MmsPart
import org.ethereumhpone.database.model.isVCard

@Composable
fun VCardBinder(
    message: Message
) {
    val vCards = remember { message.parts.filter(MmsPart::isVCard) }

    Column(
        modifier = Modifier.padding(5.dp)
    ) {
        vCards.forEach { card ->
            val vCard = LocalContext.current.contentResolver.openInputStream(card.getUri())
                .use { Ezvcard.parse(it).first() }
            VCard(vCard.getDisplayName() ?: "" ,  message.isMe())
        }
    }

    //TODO: Add ActionClick for vCard
}

@Composable
private fun VCard(name: String, isMe: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isMe) Color(0xFF8C7DF7) else Color.DarkGray)
            .padding(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "",
            tint = Color.White
        )

        Column {
            Text(text = name, fontSize = 18.sp, color = Color.White)
            Text(text = "Contact card", fontSize = 14.sp, color = Color.LightGray)
        }
    }
}


@Preview
@Composable
fun PreviewVCard() {
    VCard("Nicola Ceornea", true)
}