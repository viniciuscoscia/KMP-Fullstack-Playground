package com.viniciuscoscia.kmpfullstackplayground.intents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #6 (reverse direction) — an **intent-filter** in the manifest makes this Activity openable
 * from outside the app via the deep link `androidbasics://intents`. Test it from a terminal:
 *
 * ```
 * adb shell am start -a android.intent.action.VIEW -d "androidbasics://intents/hello"
 * ```
 *
 * The URI that opened us arrives in `intent.data`.
 */
class DeepLinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = intent.data?.toString() ?: "Opened directly (no deep-link data)."
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "Deep link") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Opened with URI:", style = MaterialTheme.typography.labelMedium)
                        Text(data, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
