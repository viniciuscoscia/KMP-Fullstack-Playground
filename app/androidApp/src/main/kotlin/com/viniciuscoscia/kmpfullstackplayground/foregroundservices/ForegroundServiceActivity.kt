package com.viniciuscoscia.kmpfullstackplayground.foregroundservices

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #8 — Foreground Services (controller UI).
 *
 * Buttons send explicit Intents (carrying a START/STOP action) to [RunningService]. On Android 13+
 * the ongoing notification only appears if the user granted `POST_NOTIFICATIONS`, so we request it
 * before starting the service.
 */
class ForegroundServiceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "8 · Foreground Service") {
                    ForegroundServiceScreen()
                }
            }
        }
    }
}

@Composable
private fun ForegroundServiceScreen() {
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* granted or not — the service still runs; only its notification depends on this */ }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Start the stopwatch service; its notification counts up every second and keeps running " +
                "when you leave the app. Stop it to remove the notification.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                if (shouldRequestNotificationPermission(context)) {
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, RunningService::class.java)
                        .setAction(RunningService.Actions.START.toString()),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Start service") }

        Button(
            onClick = {
                context.startService(
                    Intent(context, RunningService::class.java)
                        .setAction(RunningService.Actions.STOP.toString()),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Stop service") }
    }
}

private fun shouldRequestNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
