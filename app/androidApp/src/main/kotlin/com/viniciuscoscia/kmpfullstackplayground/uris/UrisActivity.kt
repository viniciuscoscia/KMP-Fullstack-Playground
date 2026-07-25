package com.viniciuscoscia.kmpfullstackplayground.uris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

// Topic #10 — Uris. TEMPLATE: to be implemented.
class UrisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "10 · Uris") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
