package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
fun NewMessagesDivider(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp)
    ) {
        Divider(Modifier.weight(1f),thickness= 1.dp,dgenTurqoise.copy(0.5f))
        Text(
            text = "$count new message${if (count > 1) "s" else ""}".uppercase(),
            style = TextStyle(
                fontFamily = SpaceMono,
                color = dgenTurqoise,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(Modifier.weight(1f),thickness= 1.dp,dgenTurqoise.copy(0.5f))
    }
}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun NewMessagePreview(){
    NewMessagesDivider(8)
}