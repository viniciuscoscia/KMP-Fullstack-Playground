package com.viniciuscoscia.kmpfullstackplayground.activitiesinternals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 3 — Activities Under the Hood. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. The ActivityTaskManagerService
 * 2. Saved Instance State
 * 3. The Activity Result Registry
 */
class ActivitiesInternalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "3 · Activities Under the Hood") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
