package com.viniciuscoscia.kmpfullstackplayground.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * A single Material 3 theme wrapper reused by every topic Activity so the whole showcase looks
 * consistent. It switches between a light and dark color scheme based on the system setting — the
 * "Resources & Qualifiers" topic (#5) explains how Android decides which one to use via the `-night`
 * resource qualifier.
 */
@Composable
fun PlaygroundTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content,
    )
}
