package com.viniciuscoscia.kmpfullstackplayground.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 9 — Battery Management. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. What drains battery the most?
 * 2. Doze Mode & App Standby
 * 3. WorkManager
 */
class BatteryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "9 · Battery Management") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
