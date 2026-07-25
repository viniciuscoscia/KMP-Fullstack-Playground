package com.viniciuscoscia.kmpfullstackplayground.broadcastreceiver

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #7 — Broadcasts & Broadcast Receivers.
 *
 * A broadcast is a system-wide (or app-wide) event; a `BroadcastReceiver` reacts to it. Receivers
 * can be registered two ways:
 * - **Manifest-declared** — can wake even when the app isn't running (limited for implicit system
 *   broadcasts since Android 8). [AirPlaneModeReceiver] is declared in the manifest.
 * - **Context-registered** — active only while registered; ideal for UI. This screen registers and
 *   unregisters via a [DisposableEffect] tied to the composition's lifecycle.
 *
 * Toggle airplane mode to see the system receiver fire, or tap the button to send our own private
 * broadcast to [CustomReceiver].
 */
class BroadcastActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "7 · Broadcasts") {
                    BroadcastScreen()
                }
            }
        }
    }
}

@Composable
private fun BroadcastScreen() {
    val context = LocalContext.current

    // Register context receivers for as long as this screen is composed, then clean them up.
    DisposableEffect(Unit) {
        val airplaneReceiver = AirPlaneModeReceiver()
        val customReceiver = CustomReceiver()
        ContextCompat.registerReceiver(
            context, airplaneReceiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED, // system broadcasts must be received as exported
        )
        ContextCompat.registerReceiver(
            context, customReceiver,
            IntentFilter(CustomReceiver.ACTION_CUSTOM),
            ContextCompat.RECEIVER_NOT_EXPORTED, // our own action — keep it app-private
        )
        onDispose {
            context.unregisterReceiver(airplaneReceiver)
            context.unregisterReceiver(customReceiver)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Toggle Airplane mode in Quick Settings — the system receiver shows a Toast. " +
                "Or send an app-private broadcast:",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                val intent = Intent(CustomReceiver.ACTION_CUSTOM)
                    .setPackage(context.packageName) // keep the broadcast inside our app
                    .putExtra(CustomReceiver.EXTRA_MESSAGE, "Hello from BroadcastActivity")
                context.sendBroadcast(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Send custom broadcast") }
    }
}
