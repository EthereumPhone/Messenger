package org.ethereumhpone.chat.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.R
import org.ethereumphone.dgenlibrary.components.SecondaryScreenHeader
import org.ethereumphone.dgenlibrary.theme.dgenBlack
import org.ethereumphone.dgenlibrary.theme.dgenOcean

/**
 *
 * val focusManager = LocalFocusManager.current
 * val interactionSource = remember { MutableInteractionSource() }
 *
 *
 */


@Composable
fun GlobeBackground(
    modifier: Modifier = Modifier,
    focusManager: FocusManager,
    interactionSource: MutableInteractionSource = MutableInteractionSource() ,
    primaryColor: Color,
    title: String = "",
    onDismiss: () -> Unit,
    content: (@Composable () -> Unit),
){

    val context = LocalContext.current
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    // Full-screen overlay with globe animation background (matching WalletManager structure)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
            }
    )
    {
        // Globe wireframe animation background - direct sibling of content
        AsyncImage(
            modifier = Modifier
                .alpha(pulseOpacity)
                .offset(x = 250.dp, y = 20.dp)
                .scale(1.3f)
                .aspectRatio(1f),
            imageLoader = gifEnabledLoader,
            model = R.drawable.globe_wireframe,
            contentDescription = null,
            colorFilter = ColorFilter.tint(primaryColor)
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SecondaryScreenHeader(
                title = title,
                primaryColor = primaryColor,
                onDismiss = onDismiss
            )
            Box(
                Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }

    }

}