package com.example.dgenlibrary.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

@Immutable
data class DgenColors(
    val dgenBlack: Color,
    val dgenWhite: Color,
    val dgenGray: Color,
    val dgenRed: Color,
    val dgenDarkBlack: Color,
    val dgenGreen: Color,
    val dgenAqua: Color,
    val dgenOrche: Color,
    val dgenOcean: Color,
    val dgenTurqoise: Color,
    val dgenBurgendy: Color,
)

@Immutable
data class DgenTypography(
    val header0: TextStyle,
    val header1: TextStyle,
    val header2: TextStyle,
    val header3: TextStyle,
    val body2: TextStyle,
    val body1: TextStyle,
    val button: TextStyle,
    val label: TextStyle
)

@Immutable
data class DgenElevation(
    val default: Dp,
    val pressed: Dp
)


@Immutable
data class DgenDimension(
    val IconSize: Dp,
    val IconButtonSize: Dp,

)


val LocalCustomColors = staticCompositionLocalOf {
    DgenColors(
        dgenBlack = Color.Unspecified,
        dgenWhite = Color.Unspecified,
        dgenGray = Color.Unspecified,
        dgenRed = Color.Unspecified,
        dgenDarkBlack = Color.Unspecified,
        dgenGreen = Color.Unspecified,
        dgenAqua = Color.Unspecified,
        dgenOrche = Color.Unspecified,
        dgenOcean = Color.Unspecified,
        dgenTurqoise = dgenTurqoise,
        dgenBurgendy = dgenBurgendy
    )
}
val LocalCustomTypography = staticCompositionLocalOf {
    DgenTypography(
        header0 = TextStyle.Default,
        header1 = TextStyle.Default,
        header2 = TextStyle.Default,
        header3 = TextStyle.Default,
        body2 = TextStyle.Default,
        body1 = TextStyle.Default,
        button = TextStyle.Default,
        label = TextStyle.Default,
    )
}
val LocalCustomElevation = staticCompositionLocalOf {
    DgenElevation(
        default = Dp.Unspecified,
        pressed = Dp.Unspecified
    )
}

val LocalCustomDimension = staticCompositionLocalOf {
    DgenDimension(
        IconSize = Dp.Unspecified,
        IconButtonSize = Dp.Unspecified
    )
}

@Composable
fun DgenTheme(
    /* ... */
    content: @Composable () -> Unit
) {
    val customColors = DgenColors(
        dgenBlack = dgenBlack,
        dgenWhite = dgenWhite,
        dgenGray = dgenGray,
        dgenRed = dgenRed,
        dgenDarkBlack = dgenDarkBlack,
        dgenGreen = dgenGreen,
        dgenAqua = dgenAqua,
        dgenOrche = dgenOrche,
        dgenOcean = dgenOcean,
        dgenTurqoise = dgenTurqoise,
        dgenBurgendy = dgenBurgendy
    )
    val customTypography = DgenTypography(
        header0 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 128.sp,
            lineHeight = 128.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header1 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 72.sp,
            lineHeight = 72.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header2 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 48.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header3 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        body2 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        body1 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        button = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenBlack,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        label = TextStyle(
            fontFamily = SpaceMono,
            color = dgenWhite,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
    )
    val customElevation = DgenElevation(
        default = 4.dp,
        pressed = 8.dp
    )

    val customDimension = DgenDimension(
        IconSize = IconSize,
        IconButtonSize = IconButtonSize
    )

    CompositionLocalProvider(
        LocalCustomColors provides customColors,
        LocalCustomTypography provides customTypography,
        LocalCustomElevation provides customElevation,
        LocalCustomDimension provides customDimension,
        content = content
    )
}

// Use with eg. CustomTheme.elevation.small
object DgenTheme {
    val colors: DgenColors
        @Composable
        get() = LocalCustomColors.current
    val typography: DgenTypography
        @Composable
        get() = LocalCustomTypography.current
    val elevation: DgenElevation
        @Composable
        get() = LocalCustomElevation.current
    val dimensions: DgenDimension
        @Composable
        get() = LocalCustomDimension.current
}