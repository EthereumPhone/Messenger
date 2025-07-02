package org.ethereumphone.dgenlibrary.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.ImageLoader


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.neonOpacity
import com.example.dgenlibrary.ui.theme.pulseOpacity
import org.ethereumphone.dgenlibrary.R

@Composable
fun InformationScreen(
    modifier: Modifier = Modifier,
    gifEnabledLoader: ImageLoader,
    gifSize: Dp = 275.dp,
    primaryColor: Color,
    text: String
){
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
        ) {
            AsyncImage(
                imageLoader = gifEnabledLoader,
                model = R.drawable.wireframe_torus,
                contentDescription = null,
                modifier = Modifier.size(gifSize),
                colorFilter = ColorFilter.tint(primaryColor.copy(pulseOpacity))

            )

            Text(
                text = text,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = primaryColor.copy(neonOpacity),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.width(300.dp)
            )
        }
    }
}