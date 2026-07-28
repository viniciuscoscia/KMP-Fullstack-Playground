package com.viniciuscoscia.kmpfullstackplayground.viewsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 5 — UI & The View System. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. Android's Rendering Pipeline
 * 2. The View System
 * 3. Jetpack Compose
 */
class ViewSystemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "5 · UI & The View System") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
