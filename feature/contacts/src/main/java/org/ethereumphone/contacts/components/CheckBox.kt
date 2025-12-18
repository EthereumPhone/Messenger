package org.ethereumphone.contacts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

@Composable
fun CheckBox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    borderWidth: Dp = 2.dp,
    primaryColor: Color = dgenTurqoise,
    borderColor: Color = primaryColor,
    fillColor: Color = primaryColor,
    checkColor: Color = dgenOcean
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = if (checked) fillColor else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = checkColor,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/*

 */
@Preview
@Composable
fun MyScreenPreview() {
    var isChecked by remember { mutableStateOf(false) }

    CheckBox(
        checked = isChecked,
        onCheckedChange = { isChecked = it },
        modifier = Modifier.padding(16.dp)
    )
}
