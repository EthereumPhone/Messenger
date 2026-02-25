package com.messenger.terminalsdk

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.messenger.terminalsdk.MiniDisplayTouchHandler

class TerminalSDK(private val context: Context) {

    /* ----------------------------------------------------------------------------------------- */
    /*  Reflection plumbing                                                                      */
    /* ----------------------------------------------------------------------------------------- */

    private val cls = Class.forName(PROXY_CLS)

    private val mGetInstance = cls.getDeclaredMethod("getInstance")
    private val mScreenOn    = cls.getDeclaredMethod("screenOn")
    private val mScreenOff   = cls.getDeclaredMethod("screenOff")
    private val mIsOn        = cls.getDeclaredMethod("isScreenOn")
    private val mRefresh     = cls.getDeclaredMethod(
        "refresh",               // Kotlin wrapper name
        Bitmap::class.java,      // arg0: Bitmap
        Int::class.javaPrimitiveType  // arg1: int id
    )
    private val mResume      = cls.getDeclaredMethod("resume", Int::class.javaPrimitiveType)

    /* constants fetched reflectively so we don't hard-code */
    val ID_STATUSBAR     = cls.getField("ID_STATUSBAR").getInt(null)
    val ID_INCOMINGCALL  = cls.getField("ID_INCOMINGCALL").getInt(null)
    val ID_NOTIFICATIONS = cls.getField("ID_NOTIFICATIONS").getInt(null)
    val ID_CLOCK         = cls.getField("ID_CLOCK").getInt(null)
    val ID_GOOGLEBYE     = cls.getField("ID_GOOGLEBYE").getInt(null)
    val ID_PERSISTENT    = cls.getField("ID_PERSISTENT").getInt(null)

    /* singleton instance inside the proxy, may be null if service missing */
    private val proxy: Any? = mGetInstance.invoke(null)

    /* reference to current touch handler */
    private var miniDisplayTouchHandler: MiniDisplayTouchHandler? = null

    private val methodMutex = Mutex()

    // Indicates whether a bitmap has been pushed since the last resume.
    private var bitmapPushed: Boolean = false

    private val scope = CoroutineScope(Dispatchers.Main)

    private suspend fun <T> synchronizedBuffer(action: suspend () -> T): T {
        methodMutex.lock()
        try {
            val result = action()
            //delay(150)
            return result
        } finally {
            methodMutex.unlock()
        }
    }

    /* ----------------------------------------------------------------------------------------- */
    /*  Public façade                                                                            */
    /* ----------------------------------------------------------------------------------------- */

    /** Is the proxy (and therefore the back-screen HAL) available? */
    suspend fun isAvailable(): Boolean {
        println("ETHOSDEBUGTERMINAL isAvailable")
        return synchronizedBuffer { proxy != null }
    }

    suspend fun isScreenOn(): Boolean {
        println("ETHOSDEBUGTERMINAL isScreenOn")
        return synchronizedBuffer {
            // Some firmware versions return void/Unit. Consider any non-exceptional call as "true".
            val result = call { mIsOn.invoke(it) }
            (result as? Boolean) ?: true
        }
    }

    suspend fun refresh(bitmap: Bitmap, id: Int): Boolean {
        println("ETHOSDEBUGTERMINAL refresh id=$id")
        return synchronizedBuffer {
            var success = false
            try {
                // On some devices/firmware the underlying proxy method returns void (i.e. Unit/null).
                // Treat any successful invocation (no exception thrown) as a successful refresh.
                call { mRefresh.invoke(it, bitmap, id) }
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (success) {
                bitmapPushed = true
            }
            success
        }
    }

    suspend fun resume(id: Int) {
        println("ETHOSDEBUGTERMINAL resume id=$id, bitmapPushed=$bitmapPushed")
        synchronizedBuffer {
            try {
                call { mResume.invoke(it, id) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Reset flag so that subsequent resumes know a fresh bitmap is needed
                bitmapPushed = false
            }
        }
    }

    /* ----------------------------------------------------------------------------------------- */
    /*  Helpers                                                                                  */
    /* ----------------------------------------------------------------------------------------- */

    private suspend inline fun <T> call(crossinline block: (Any) -> T): T? =
        withContext(Dispatchers.Main) {
            proxy?.let { block(it) }
        }

    suspend fun displaySend(sendTx: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displaySend")
        // Clean up any existing touch handler first
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val sendBitmap = layoutRenderer.renderSend()

        refresh(sendBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    performHapticFeedback()
                    sendTx()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeSend() {
        println("ETHOSDEBUGTERMINAL removeSend")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    suspend fun displaySendRequest(sendRequest: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displaySendRequest")
        // Clean up any existing touch handler first
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val requestBitmap = layoutRenderer.renderSendRequest()

        refresh(requestBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    performHapticFeedback()
                    sendRequest()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeSendRequest() {
        println("ETHOSDEBUGTERMINAL removeSendRequest")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    suspend fun displayNext(onNext: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displayNext")
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val nextBitmap = layoutRenderer.renderNext()

        refresh(nextBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    performHapticFeedback()
                    onNext()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeNext() {
        println("ETHOSDEBUGTERMINAL removeNext")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    suspend fun displayConfirm(onConfirm: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displayConfirm")
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val confirmBitmap = layoutRenderer.renderConfirm()

        refresh(confirmBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { x, y, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    performHapticFeedback()
                    onConfirm()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeConfirm() {
        println("ETHOSDEBUGTERMINAL removeConfirm")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    suspend fun displayAddMember(onAddMember: () -> Unit) {
        println("ETHOSDEBUGTERMINAL displayAddMember")
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val addMemberBitmap = layoutRenderer.renderAddMember()

        refresh(addMemberBitmap, ID_PERSISTENT)

        miniDisplayTouchHandler = MiniDisplayTouchHandler(
            context,
            MiniDisplayTouchHandler.OnTouchListener { _, _, action ->
                if (action != MotionEvent.ACTION_DOWN) {
                    return@OnTouchListener
                }
                try {
                    performHapticFeedback()
                    onAddMember()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    suspend fun removeAddMember() {
        println("ETHOSDEBUGTERMINAL removeAddMember")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /**
     * Manually destroy the current touch handler
     */
    suspend fun destroyTouchHandler() {
        println("ETHOSDEBUGTERMINAL destroyTouchHandler")
        synchronizedBuffer {
            miniDisplayTouchHandler?.destroy()
            miniDisplayTouchHandler = null
        }
    }
    
    /**
     * Synchronously destroy the current touch handler.
     * This is called from Activity onDestroy() to ensure cleanup happens before the app closes.
     */
    fun destroyTouchHandlerSync() {
        println("ETHOSDEBUGTERMINAL destroyTouchHandlerSync")
        miniDisplayTouchHandler?.destroy()
        miniDisplayTouchHandler = null
        // Also clean up any static instance as a failsafe
        MiniDisplayTouchHandler.cleanupActiveInstance()
    }

    suspend fun finishScreen() {
        println("ETHOSDEBUGTERMINAL finishScreen")
        resume(ID_STATUSBAR)
        destroyTouchHandler()
    }

    /** Vibrator for haptic feedback */
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Performs haptic feedback for terminal button presses.
     * Uses EFFECT_HEAVY_CLICK to match the LongPress haptic feel used in WalletManager.
     */
    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Displays a simple black screen with the supplied [text] rendered in red and centred.
     * Uses the same dimensions as the existing `black_layout.xml` (428 × 142 px).
     *
     * Calling this will first clean up any active touch-handler and then push the
     * rendered bitmap to the mini-display using the persistent layer (ID_PERSISTENT).
     * No touch processing is installed for this view.
     */
    suspend fun displayBlackText(text: String) {
        println("ETHOSDEBUGTERMINAL displayBlackText text='$text'")
        // Remove any existing touch handling to avoid leaking receivers
        destroyTouchHandler()

        val layoutRenderer = LayoutRenderer(context)
        val bitmap = layoutRenderer.renderBlackText(text)

        // Push the bitmap to the display – keep it until explicitly cleared
        refresh(bitmap, ID_PERSISTENT)
    }
}

/* name of the real proxy class */
private const val PROXY_CLS = "android.os.FreemeProxy" 