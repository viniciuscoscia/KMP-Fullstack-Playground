package com.viniciuscoscia.kmpfullstackplayground.launchmodes

import android.content.Intent
import android.os.Bundle
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
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import com.viniciuscoscia.kmpfullstackplayground.common.findActivity

/**
 * Topic #2 — Tasks, Back Stack & Launch Modes.
 *
 * A *task* is a back stack of activities. `launchMode` controls whether starting an Activity pushes
 * a brand-new instance or reuses an existing one:
 * - **standard** — always a new instance (this screen).
 * - **singleTop** — reuses the instance only if it is already on top (see [SingleTopActivity]).
 * - **singleTask** — keeps a single instance and clears everything above it (see [SingleTaskActivity]).
 *
 * Each screen prints its `taskId` and a per-instance id; launch the different modes and watch which
 * ids change to *feel* how the back stack behaves.
 */
class LaunchModesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "2 · Launch Modes") {
                    LaunchModesScreen(label = "standard (this)")
                }
            }
        }
    }
}

/** singleTop: reused when it is already the top activity of the task. Declared in the manifest. */
class SingleTopActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme { DemoScaffold(title = "singleTop") { LaunchModesScreen(label = "singleTop") } }
        }
    }
}

/** singleTask: only one instance ever exists; relaunching clears any activities above it. */
class SingleTaskActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme { DemoScaffold(title = "singleTask") { LaunchModesScreen(label = "singleTask") } }
        }
    }
}

@Composable
private fun LaunchModesScreen(label: String) {
    val context = LocalContext.current
    val activity = context.findActivity()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Current screen: $label", style = MaterialTheme.typography.titleMedium)
                Text("taskId: ${activity?.taskId}")
                Text("instance: ${activity?.let { System.identityHashCode(it) }}")
            }
        }
        Button(
            onClick = { context.startActivity(Intent(context, LaunchModesActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Start standard") }
        Button(
            onClick = { context.startActivity(Intent(context, SingleTopActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Start singleTop") }
        Button(
            onClick = { context.startActivity(Intent(context, SingleTaskActivity::class.java)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Start singleTask") }
        Button(
            onClick = {
                // FLAG_ACTIVITY_CLEAR_TOP pops every activity above the target; SINGLE_TOP reuses the
                // existing instance instead of recreating it — together they "jump back" to the first.
                val intent = Intent(context, LaunchModesActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Back to first (CLEAR_TOP)") }
    }
}
