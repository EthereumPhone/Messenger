package org.ethereumhpone.data.manager

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.ethereumhpone.domain.manager.AppStateMonitor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes the process lifecycle so we always know whether the UI is visible. This gives us a
 * single source of truth when deciding if we should surface notifications or rely on the in-app
 * UI (which already shows the incoming message).
 */
@Singleton
class AppStateMonitorImpl @Inject constructor() : AppStateMonitor, DefaultLifecycleObserver {

    private val processLifecycle = ProcessLifecycleOwner.get()
    private val _isForeground = MutableStateFlow(
        processLifecycle.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED),
    )

    init {
        processLifecycle.lifecycle.addObserver(this)
    }

    override val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    override fun isAppInForeground(): Boolean = _isForeground.value

    override fun onStart(owner: LifecycleOwner) {
        _isForeground.value = true
    }

    override fun onStop(owner: LifecycleOwner) {
        _isForeground.value = false
    }
}

