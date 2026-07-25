package com.viniciuscoscia.kmpfullstackplayground.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Walks up the [Context] chain to find the hosting [Activity].
 *
 * A Compose `LocalContext` is not guaranteed to be an Activity — it is often a [ContextWrapper] that
 * wraps one (for theming). This is the safe, idiomatic way to reach the Activity, and it doubles as
 * a concrete example for the "What is the Context?" topic (#4): Context types wrap each other.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
