package org.ethereumhpone.chat.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.components.dgenButton

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Storage permission is required to access media files",
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontFamily = PitagonsSans,
                color = dgenTurqoise,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        dgenButton(
            onClick = onRequestPermission,
            text = "Grant Permission"
        )

    }
}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun PreviewPermissionScreen() {
    PermissionScreen {}
}