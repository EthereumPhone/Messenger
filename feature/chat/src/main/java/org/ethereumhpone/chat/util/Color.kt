package org.ethereumhpone.chat.util

import androidx.compose.ui.graphics.Color
import org.ethereumphone.dgenlibrary.theme.dgenAqua
import org.ethereumphone.dgenlibrary.theme.dgenGreen
import org.ethereumphone.dgenlibrary.theme.dgenOrche
import org.ethereumphone.dgenlibrary.theme.dgenRed
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import org.ethereumphone.dgenlibrary.theme.dgenWhite
import org.ethereumphone.dgenlibrary.theme.gunMetalCore
import org.ethereumphone.dgenlibrary.theme.lazerCore
import org.ethereumphone.dgenlibrary.theme.oceanCore
import org.ethereumphone.dgenlibrary.theme.terminalCore
import org.ethereumphone.model.Recipient
import kotlin.math.absoluteValue

// DgenTheme colors for group member names (excluding black)
private val dgenGroupColors = listOf(
    dgenRed,
    dgenGreen,
    dgenAqua,
    dgenOrche,
    dgenTurqoise,
    dgenWhite,
    lazerCore,
    terminalCore,
    oceanCore,
    gunMetalCore
)

// Hash-based color assignment: stable across app sessions for the same recipient
fun colorFor(recipient: Recipient): Color {
    val idx = (recipient.id.hashCode().absoluteValue) % dgenGroupColors.size
    return dgenGroupColors[idx]
}