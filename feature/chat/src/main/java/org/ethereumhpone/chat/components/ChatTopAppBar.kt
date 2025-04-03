package org.ethereumhpone.chat.components

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
import org.ethereumhpone.database.model.RecipientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    title: String = "",
    recipientEntities: List<RecipientEntity>,
    onTitleClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    val header = title.ifBlank {
        val displayNames = recipientEntities.map { "it.getDisplayName()" } //TODO FIX display name
        displayNames.joinToString(", ")
    }

    CenterAlignedTopAppBar(
        title = {
            Row {

                //TODO: Add icon

                Text(
                    text = header,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onTitleClicked() },
                )
            }

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



@Preview
@Composable
fun PreviewChatTopAppBar() {
    ChatTopAppBar("My chat", emptyList(), {}, {})
}

@Preview
@Composable
fun PreviewChatTopAppBarNoTitle() {
    /*
ChatTopAppBar(
        "",
        listOf(
            Recipient(address = "nicola"),
            Recipient(address = "nicola"),
            Recipient(address = "nicola"),
            Recipient(address = "nicola"),
            Recipient(address = "nicola"),
            Recipient(address = "nicola"),

        ),
        {},
        {}
    )
     */

}

