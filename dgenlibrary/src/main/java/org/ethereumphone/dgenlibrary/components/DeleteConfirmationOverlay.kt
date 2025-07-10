package org.ethereumphone.dgenlibrary.components

//region Imports
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.body1_fontSize
import com.example.dgenlibrary.ui.theme.label_fontSize
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
//endregion

@Composable
fun DeleteConfirmationOverlay(
    message: String,
    primaryColor: Color,
    secondaryColor: Color,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)
            .pointerInput(Unit) {
                // Dismiss overlay on outside tap
                detectTapGestures { onCancel() }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.width(350.dp)
        ) {
            // Confirmation text
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = body1_fontSize,
                    lineHeight = body1_fontSize,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // DELETE button
            Text(
                text = "DELETE",
                modifier = Modifier
                    .background(color = primaryColor, shape = CircleShape)
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .pointerInput(Unit) { detectTapGestures { onDelete() } },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = secondaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = label_fontSize,
                    textAlign = TextAlign.Center
                )
            )

            // CANCEL text button
            Text(
                text = "CANCEL",
                modifier = Modifier.pointerInput(Unit) { detectTapGestures { onCancel() } },
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = label_fontSize,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
} 