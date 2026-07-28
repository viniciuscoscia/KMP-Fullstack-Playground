package com.viniciuscoscia.kmpfullstackplayground.osarchitecture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Mirrors Philipp Lackner's reference `MyViewModel` (AndroidInternals repo,
 * `system-architecture/thread-looper` branch) — a plain class, not an `androidx.lifecycle.ViewModel`,
 * used only to hold a counter for the `Dispatchers.Main.immediate` demo in [OsArchitectureActivity].
 */
class MyViewModel {

    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    fun increment() {
        _counter.update { it + 1 }
    }
}
