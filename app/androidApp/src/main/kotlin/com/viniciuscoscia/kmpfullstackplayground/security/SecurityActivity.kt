package com.viniciuscoscia.kmpfullstackplayground.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 7 — Android's Security System. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. Sandboxing & SELinux
 * 2. Internal & External Storage
 * 3. Rooting
 * 4. The Android Keystore, TEE & StrongBox
 * 5. APKs, AABs & App Signing
 * 6. Android's Permission Model
 * 7. Protecting Against Reverse Engineering
 */
class SecurityActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "7 · Android's Security System") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
