package com.viniciuscoscia.kmpfullstackplayground.ipc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 6 — Inter Process Communication (IPC). TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. Intents
 * 2. Broadcasts
 * 3. Local Bound Services
 * 4. IPC Bound Services Via Messenger API
 * 5. AIDL
 * 6. Content Providers
 */
class IpcActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "6 · Inter Process Communication (IPC)") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
