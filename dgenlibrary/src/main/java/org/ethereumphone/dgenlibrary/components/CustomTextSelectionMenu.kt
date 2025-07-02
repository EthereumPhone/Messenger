package org.ethosmobile.contacts.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Stable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import org.ethereumphone.dgenlibrary.SystemColorManager
import org.ethereumphone.dgenlibrary.theme.DgenTheme
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise

/**
 * State holder for custom text toolbar
 */
@Stable
class CustomTextToolbarState(
    private val primaryColor: Color = dgenTurqoise
) {
    var isShowing by mutableStateOf(false)
        private set
    var menuRect by mutableStateOf(Rect.Zero)
        private set
    var onCopy: (() -> Unit)? by mutableStateOf(null)
        private set
    var onPaste: (() -> Unit)? by mutableStateOf(null)
        private set
    var onCut: (() -> Unit)? by mutableStateOf(null)
        private set
    var onSelectAll: (() -> Unit)? by mutableStateOf(null)
        private set

    fun show(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        menuRect = rect
        onCopy = onCopyRequested
        onPaste = onPasteRequested
        onCut = onCutRequested
        onSelectAll = onSelectAllRequested
        isShowing = true
    }

    fun hide() {
        isShowing = false
    }
}

/**
 * Custom Text Toolbar implementation with styled menu
 */
class CustomTextToolbar(
    private val state: CustomTextToolbarState
) : TextToolbar {
    
    override val status: TextToolbarStatus
        get() = if (state.isShowing) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

    override fun hide() {
        state.hide()
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        // Only show if not already showing to prevent flickering
        if (!state.isShowing) {
            state.show(rect, onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
        }
    }
}

/**
 * Composable function to render the custom text selection menu
 */
@Composable
fun CustomTextSelectionMenuContent(state: CustomTextToolbarState) {
    // Directly use the state without additional layers to prevent blinking
    if (state.isShowing) {
        // Use DisposableEffect to ensure proper cleanup
        DisposableEffect(state.isShowing) {
            onDispose {
                // Cleanup if needed
            }
        }
        
        CustomSelectionMenu(
            rect = state.menuRect,
            onCopy = state.onCopy,
            onPaste = state.onPaste,
            onCut = state.onCut,
            onSelectAll = state.onSelectAll,
            onDismiss = { state.hide() },
            primaryColor = SystemColorManager.primaryColor
        )
    }
}

@Composable
fun CustomSelectionMenu(
    rect: Rect,
    onCopy: (() -> Unit)?,
    onPaste: (() -> Unit)?,
    onCut: (() -> Unit)?,
    onSelectAll: (() -> Unit)?,
    onDismiss: () -> Unit,
    primaryColor: Color = SystemColorManager.primaryColor
) {
    // Get screen dimensions
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Menu dimensions (approximate)
    val menuWidth = 200.dp
    val menuHeight = 50.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }
    val menuHeightPx = with(density) { menuHeight.toPx() }
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val screenHeightPx = with(density) { screenHeight.toPx() }
    
    // Calculate horizontal position
    var xOffset = if (rect.width > 0) {
        // Center the menu over the selection
        (rect.left + rect.width / 2 - menuWidthPx / 2).toInt()
    } else {
        // Position at cursor for paste on empty field
        (rect.left - menuWidthPx / 2).toInt()
    }
    
    // Ensure menu stays within screen bounds horizontally
    xOffset = xOffset.coerceIn(
        minimumValue = 10, // 10px padding from left edge
        maximumValue = (screenWidthPx - menuWidthPx - 10).toInt() // 10px padding from right edge
    )
    
    // Calculate vertical position
    val spaceAbove = rect.top
    val spaceBelow = screenHeightPx - rect.bottom
    val preferredSpacing = with(density) { 8.dp.toPx() } // Space between menu and text
    
    val yOffset: Int
    val verticalAlignment: Alignment.Vertical
    
    if (spaceAbove >= menuHeightPx + preferredSpacing) {
        // Place above if there's enough space
        yOffset = (rect.top - menuHeightPx - preferredSpacing).toInt()
        verticalAlignment = Alignment.Top
    } else if (spaceBelow >= menuHeightPx + preferredSpacing) {
        // Place below if there's enough space
        yOffset = (rect.bottom + preferredSpacing).toInt()
        verticalAlignment = Alignment.Top
    } else {
        // If neither has enough space, place where there's more room
        if (spaceAbove > spaceBelow) {
            yOffset = 10 // Near top of screen
            verticalAlignment = Alignment.Top
        } else {
            yOffset = (screenHeightPx - menuHeightPx - 10).toInt() // Near bottom
            verticalAlignment = Alignment.Top
        }
    }
    
    val secondaryColor = SystemColorManager.secondaryColor
    
    Popup(
        alignment = Alignment.TopStart,
        offset = androidx.compose.ui.unit.IntOffset(xOffset, yOffset),
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = false, // Prevent focus stealing
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .background(secondaryColor, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = secondaryColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Custom styled menu buttons
                onCut?.let {
                    TextSelectionMenuButton(
                        text = "CUT",
                        onClick = {
                            it()
                            onDismiss()
                        },
                        primaryColor = primaryColor
                    )
                }
                
                onCopy?.let {
                    TextSelectionMenuButton(
                        text = "COPY",
                        onClick = {
                            it()
                            onDismiss()
                        },
                        primaryColor = primaryColor
                    )
                }
                
                onPaste?.let {
                    TextSelectionMenuButton(
                        text = "PASTE",
                        onClick = {
                            it()
                            onDismiss()
                        },
                        primaryColor = primaryColor
                    )
                }
                
                onSelectAll?.let {
                    TextSelectionMenuButton(
                        text = "ALL",
                        onClick = {
                            it()
                            onDismiss()
                        },
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TextSelectionMenuButton(
    text: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = primaryColor
        )
    ) {
        Text(
            text = text,
            style = DgenTheme.typography.label,
            color = primaryColor
        )
    }
}

/**
 * Alternative: Completely custom implementation without system menu
 */
@Composable
fun CustomTextFieldWithCustomMenu() {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    // Example of handling copy/paste manually
    val handleCopy: (String) -> Unit = { text ->
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }
    
    val handlePaste: () -> String? = {
        clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
    }
    
    // Your custom implementation here
}

/**
 * Option 3: Theme-based customization
 * Add this to your theme to style the default selection menu
 */
// In your theme:
// <style name="CustomTextSelectionMenu" parent="@android:style/Widget.Material.ActionMode">
//     <item name="android:background">@color/dgen_black</item>
//     <item name="android:titleTextStyle">@style/CustomActionModeTitle</item>
// </style>
//
// <style name="CustomActionModeTitle" parent="@android:style/TextAppearance.Material.Widget.ActionMode.Title">
//     <item name="android:textColor">@color/dgen_turqoise</item>
//     <item name="android:fontFamily">@font/pitagons_sans</item>
// </style> 