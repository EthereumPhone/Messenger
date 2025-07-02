package org.ethereumphone.dgenlibrary.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun GoToBottomFab(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    Surface(
        modifier = modifier.size(40.dp).pointerInput(Unit) {
            detectTapGestures {
                onClick()
            }
        },
        shape = CircleShape,
        color = dgenOcean,
        elevation = 4.dp,
    ) {
        Icon(
            Icons.Outlined.ArrowDownward,
            contentDescription = "Downward Arrow",
            tint = dgenTurqoise,
            modifier = Modifier.size(16.dp).padding(8.dp)
        )
    }

}

@Composable
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
fun GoToBottomButtonPreview(){
    GoToBottomFab()
}