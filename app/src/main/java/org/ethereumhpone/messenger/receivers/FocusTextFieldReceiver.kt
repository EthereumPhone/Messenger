package org.ethereumhpone.messenger.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.ethereumhpone.common.util.TextFieldFocusManager

/**
 * BroadcastReceiver that handles the FOCUS_TEXT_FIELD action from the OS.
 * When the user long-presses the F2 button, the OS sends this broadcast to request
 * the foreground app to focus its main text field before voice input begins.
 */
class FocusTextFieldReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FocusTextField"

        // Result codes to return to the OS
        const val FOCUS_RESULT_SUCCESS = 1
        const val FOCUS_RESULT_NO_TEXT_FIELD = 0
        const val FOCUS_RESULT_FAILED = -1
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received FOCUS_TEXT_FIELD broadcast")

        if (!TextFieldFocusManager.hasFocusCallback()) {
            Log.d(TAG, "No focus callback registered")
            resultCode = FOCUS_RESULT_NO_TEXT_FIELD
            return
        }

        try {
            val focused = TextFieldFocusManager.requestFocus()

            if (focused) {
                Log.i(TAG, "Successfully focused text field")
                resultCode = FOCUS_RESULT_SUCCESS
                resultData = "focused"
            } else {
                Log.w(TAG, "Failed to focus text field")
                resultCode = FOCUS_RESULT_FAILED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error focusing text field", e)
            resultCode = FOCUS_RESULT_FAILED
        }
    }
}

