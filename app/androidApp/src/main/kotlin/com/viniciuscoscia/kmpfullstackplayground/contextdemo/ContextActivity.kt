package com.viniciuscoscia.kmpfullstackplayground.contextdemo

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.R
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #4 — What is the Context?
 *
 * A `Context` is the handle to everything app-related: resources, assets, system services, starting
 * activities, and package info. Two kinds matter most:
 * - **Application context** — lives for the whole process. Use it for things that outlive a screen
 *   (singletons, WorkManager, a long-lived receiver) so you don't leak an Activity.
 * - **Activity context** — tied to one screen and carries the correct theme, so use it for UI
 *   (dialogs, inflating views). Holding an Activity context in a static field **leaks** the whole
 *   screen when it is destroyed.
 */
class ContextActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "4 · Context") {
                    ContextScreen()
                }
            }
        }
    }
}

@Composable
private fun ContextScreen() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoCard("Package name", context.packageName)
        InfoCard("App label (from resources)", context.getString(R.string.app_name))
        InfoCard("Activity context === Application context?", "${context === context.applicationContext}")
        InfoCard("Has PowerManager service?", "${context.getSystemService(Context.POWER_SERVICE) != null}")
        Button(
            onClick = {
                // A Toast is UI, but it does not need the Activity's theme, so the Application
                // context is the safe, leak-free choice here.
                Toast.makeText(context.applicationContext, "Sent using the Application context", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Show a Toast (application context)") }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
