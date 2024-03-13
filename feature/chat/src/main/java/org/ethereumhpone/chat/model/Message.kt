package org.ethereumhpone.chat.model

import androidx.compose.runtime.Immutable
import org.ethereumhpone.chat.R

@Immutable
data class MockMessage(
    val author: String,
    val content: String,
    val timestamp: String,
    val image: Int? = null,
    val authorImage: Int = if (author == "me") R.drawable.ethos_placeholder else R.drawable.ethos_placeholder,
    val isTx: Boolean = false,
    val amount: Double = 0.0,
    val chainId: Int = 1,

)
