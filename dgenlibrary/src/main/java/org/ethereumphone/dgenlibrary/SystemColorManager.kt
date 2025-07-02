package org.ethereumphone.dgenlibrary

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.ethereumphone.dgenlibrary.theme.gunMetalCore
import org.ethereumphone.dgenlibrary.theme.gunMetalForge
import org.ethereumphone.dgenlibrary.theme.lazerBurn
import org.ethereumphone.dgenlibrary.theme.lazerCore
import org.ethereumphone.dgenlibrary.theme.oceanAbyss
import org.ethereumphone.dgenlibrary.theme.oceanCore
import org.ethereumphone.dgenlibrary.theme.orcheAsh
import org.ethereumphone.dgenlibrary.theme.orcheCore
import org.ethereumphone.dgenlibrary.theme.terminalCore
import org.ethereumphone.dgenlibrary.theme.terminalHack

/**
 * Manages two dynamic colors (Primary & Secondary),
 * which can be read from the system accent colors when the app starts.
 * The colors are held as `mutableStateOf`, so that any change
 * automatically triggers recomposition in @Composable callers.
 */
object SystemColorManager {

    private val DEFAULT_PRIMARY = lazerCore
    private val DEFAULT_SECONDARY = lazerBurn
    private val DEFAULT_TERITARY = Color(0xFF820303)

    /** Current primary color */
    var primaryColor by mutableStateOf(DEFAULT_PRIMARY)
        private set

    /** Current secondary color */
    var secondaryColor by mutableStateOf(DEFAULT_SECONDARY)
        private set

    /** Current openGL color */
    var openGLColor by mutableStateOf(DEFAULT_TERITARY)
        private set

    /**
     * Reads the current system accent color and updates the fields.
     * Should be executed, for example, in the `LaunchedEffect` of a screen.
     */
    fun refresh(context: Context) {
        // Android stores the accent color in Secure Settings from Android 12 onwards.
        // The fallback is an intense red if no entry is present.
        val accentInt = Settings.Secure.getInt(
            context.contentResolver,
            "systemui_accent_color",
            DEFAULT_PRIMARY.toArgb()
        )

        val accentColor = Color(accentInt)
        Log.d("SystemColorManager","accentInt: $accentInt, accentColor: $accentColor")

        //Decide the colorway
        when(accentInt){
            //TERMINAL
            -13510400 -> {
                primaryColor = terminalCore
                secondaryColor = terminalHack
                openGLColor = Color(0xFF186103)
            }
            //LAZER
            -131072 -> {
                primaryColor = lazerCore
                secondaryColor = lazerBurn
                openGLColor = Color(0xFF820303)
            }
            //OCEAN
            -16718593 -> {
                primaryColor = oceanCore
                secondaryColor = oceanAbyss
                openGLColor = Color(0xFF037582)
            }
            //ORCHE
            -1012183 -> {
                primaryColor = orcheCore
                secondaryColor = orcheAsh
                openGLColor = Color(0xFF7B4A17)
            }

            //GUNMETAL
            -3618616 -> {
                primaryColor = gunMetalCore
                secondaryColor = gunMetalForge
                openGLColor = Color(0xFF676767)
            }

            else -> {
                primaryColor = lazerCore
                secondaryColor = lazerBurn
                openGLColor = Color(0xFF820303)
            }
        }

        //-13510400 - Green
        // -131072 - Red
        // -16718593 - blue
        // -1012183 - orange
        // -3618616 - Gray
    }

    /**
     * Allows to manually override the colors (e.g. for testing).
     */
    fun setColors(primary: Color, secondary: Color) {
        primaryColor = primary
        secondaryColor = secondary
    }
} 