package org.ethereumhpone.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumhpone.chat.RecipientUiState
import org.ethereumhpone.database.model.RecipientEntity
import org.ethereumphone.model.Contact
import org.ethereumphone.model.Recipient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    title: String = "",
    recipientUiState: RecipientUiState,
    onTitleClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    when(recipientUiState) {
        is RecipientUiState.Success -> {
            val recipients = recipientUiState.recipients

            val header = title.ifBlank {
                val displayNames = recipients.map { "it.getDisplayName()" } //TODO FIX display name
                displayNames.joinToString(", ")
            }

            CenterAlignedTopAppBar(
                modifier = Modifier.background(Color.Green),
                title = {

                        Text(
                            text = header,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onTitleClicked() },
                        )
                    
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            "Back navigation",
                            tint = Color.White,
                        ) }

                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )



        }
        else -> {}
    }



}



@Preview
@Composable
fun PreviewChatTopAppBar() {
    ChatTopAppBar("My chat", RecipientUiState.Success(
        listOf(
            Recipient(
                id = "userB",
                address = "0xDeF456HodlGuyWallet",
                ens = "hodl.eth",
                contact = Contact("lk2", "Bob", null, "0x456")
            )
        )
    ), {}, {})
}
