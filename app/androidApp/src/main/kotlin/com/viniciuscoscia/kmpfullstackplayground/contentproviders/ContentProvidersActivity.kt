package com.viniciuscoscia.kmpfullstackplayground.contentproviders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

// Topic #11 — Content Providers. TEMPLATE: to be implemented.
class ContentProvidersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "11 · Content Providers") {
                    Text("Template — to be implemented.")
                }
            }
        }
    }
}
