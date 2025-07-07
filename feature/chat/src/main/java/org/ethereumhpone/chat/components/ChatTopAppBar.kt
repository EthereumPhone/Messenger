package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumhpone.chat.RecipientUiState
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Recipient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    title: String = "",
    recipientUiState: RecipientUiState,
    onTitleClicked: () -> Unit,
    onBackClicked: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier,

    ) {
    when(recipientUiState) {
        is RecipientUiState.Success -> {
            val recipients = recipientUiState.recipients

            val header = title.ifBlank {
                val displayNames = recipients.map { "${it.contact?.name}" } //TODO FIX display name
                displayNames.joinToString(", ")
            }

            Row(
                modifier = modifier
                    .background(dgenBlack)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(onClick = onBackClicked) {
                    Icon(
                        painter = painterResource(R.drawable.backicon),
                        contentDescription = "BackButton",
                        modifier = modifier.size(24.dp),
                        tint = primaryColor
                    )
                }
                Text(
                    text = header,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                        .widthIn(min = 10.dp, max = 250.dp)
                        .clickable { onTitleClicked() },
                )
                IconButton(onClick = onTitleClicked) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "BackButton",
                        modifier = modifier.size(24.dp),
                        tint = primaryColor
                    )
                }

            }



        }
        else -> {}
    }



}



@Preview
@Composable
fun PreviewChatTopAppBar() {
    ChatTopAppBar("Alex Lynn", RecipientUiState.Success(
        listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        )
    ), {}, {}, Color.Red)
}
