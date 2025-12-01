package org.ethereumhpone.domain.manager

import kotlinx.coroutines.flow.StateFlow

/**
 * Tracks whether the app process is currently in the foreground (visible to the user) or has been
 * backgrounded. This is used to avoid spamming the user with duplicate notifications when they are
 * already looking at the conversation inside the app, while still ensuring background sync/stream
 * events surface notifications when the app is minimized.
 */
interface AppStateMonitor {

    /**
     * Emits true while the process lifecycle is at least STARTED (foreground) and false once it
     * transitions to the background.
     */
    val isForeground: StateFlow<Boolean>

    /**
     * Convenience accessor for the latest known foreground/background state.
     */
    fun isAppInForeground(): Boolean
}

