package com.viniciuscoscia.kmpfullstackplayground.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Topic #3 — ViewModels & Configuration Changes.
 *
 * A [ViewModel] outlives configuration changes (rotation, dark-mode toggle, ...): the framework
 * keeps the same instance while the Activity is destroyed and recreated, so state held here is not
 * lost. Backing the count with a [SavedStateHandle] goes one step further and also survives
 * **process death** (the system killing the app in the background) — the handle is persisted to a
 * Bundle just like [android.app.Activity.onSaveInstanceState].
 */
class CounterViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _count = MutableStateFlow(savedStateHandle[KEY_COUNT] ?: 0)
    val count = _count.asStateFlow()

    fun increment() = update(_count.value + 1)
    fun decrement() = update(_count.value - 1)

    private fun update(newValue: Int) {
        _count.value = newValue
        // Persist so the value also survives process death, not just configuration changes.
        savedStateHandle[KEY_COUNT] = newValue
    }

    private companion object {
        const val KEY_COUNT = "count"
    }
}
