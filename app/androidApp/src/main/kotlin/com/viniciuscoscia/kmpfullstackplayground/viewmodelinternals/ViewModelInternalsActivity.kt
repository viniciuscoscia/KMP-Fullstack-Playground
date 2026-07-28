package com.viniciuscoscia.kmpfullstackplayground.viewmodelinternals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Android Internals section 4 — ViewModels. TEMPLATE: to be implemented.
 *
 * Lessons in this section:
 * 1. The ViewModel Lifecycle
 * 2. Androidx ViewModel Internals
 * 3. Process Death & SavedStateHandle
 */
class ViewModelInternalsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "4 · ViewModels") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
