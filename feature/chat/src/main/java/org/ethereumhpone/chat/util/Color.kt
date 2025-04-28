package org.ethereumhpone.chat.util

import androidx.compose.ui.graphics.Color
import org.ethereumphone.model.Recipient
import kotlin.math.absoluteValue

// 1. Deine 10 Pastell‑Hex‑Farben
private val pastelColors = listOf(
    "#FFB3BA", "#FFDFBA", "#FFFFBA", "#BAFFC9", "#BAE1FF",
    "#E2BAFF", "#FFC9DE", "#C9FFE5", "#E5C9FF", "#C9DFFF"
)

// 2. Hash‑basierte Zuordnung: stabil, wenn sich die Liste ändert
fun colorFor(recipient: Recipient): Color {
    // Nimm den absoluten Hash‑Wert der ID, modulo Farb‑Anzahl
    val idx = (recipient.id.hashCode().absoluteValue) % pastelColors.size
    return Color(android.graphics.Color.parseColor(pastelColors[idx]))
}