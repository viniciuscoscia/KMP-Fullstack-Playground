package com.viniciuscoscia.kmpfullstackplayground.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 8 — Memory Management. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. Heap & Garbage Collection in ART
 * 2. Memory Leaks
 */
class MemoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "8 · Memory Management") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
