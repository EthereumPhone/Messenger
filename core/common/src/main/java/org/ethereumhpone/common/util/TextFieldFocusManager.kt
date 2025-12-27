package org.ethereumhpone.common.util

/**
 * Singleton manager that bridges the FocusTextFieldReceiver and chat screens.
 * 
 * When the OS sends a FOCUS_TEXT_FIELD broadcast (e.g., when user long-presses F2
 * for voice input), the receiver calls [requestFocus] which invokes the registered
 * callback to focus the primary text field.
 */
object TextFieldFocusManager {
    
    private var focusCallback: (() -> Boolean)? = null
    
    /**
     * Register a callback that will be invoked when a focus request is received.
     * The callback should request focus on the text field and return true if successful.
     * 
     * Call this in onResume/LaunchedEffect when the screen becomes active.
     */
    fun registerFocusCallback(callback: () -> Boolean) {
        focusCallback = callback
    }
    
    /**
     * Unregister the focus callback.
     * 
     * Call this in onPause/DisposableEffect when the screen is no longer active.
     */
    fun unregisterFocusCallback() {
        focusCallback = null
    }
    
    /**
     * Request focus on the registered text field.
     * 
     * @return true if focus was successfully requested, false if no callback is registered
     *         or if the focus request failed.
     */
    fun requestFocus(): Boolean {
        return focusCallback?.invoke() ?: false
    }
    
    /**
     * Check if a focus callback is currently registered.
     */
    fun hasFocusCallback(): Boolean = focusCallback != null
}

