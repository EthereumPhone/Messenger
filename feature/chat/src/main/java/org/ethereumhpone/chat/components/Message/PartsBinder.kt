package org.ethereumhpone.chat.components.Message

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.ethereumhpone.database.model.MmsPart

@Composable
fun PartsBinder(
    mmsParts: List<MmsPart>
) {
    val groupedParts = remember { mmsParts.groupBy { mmsParts -> mmsParts.type } }




}