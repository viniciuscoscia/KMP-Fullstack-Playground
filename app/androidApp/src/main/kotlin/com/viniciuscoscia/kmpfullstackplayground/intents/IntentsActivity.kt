package com.viniciuscoscia.kmpfullstackplayground.intents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #6 — Intents & Intent Filters.
 *
 * An `Intent` is a message describing an operation:
 * - **Explicit** — you name the exact component (`Intent(this, SecondActivity::class.java)`), used
 *   to move between your own screens. [rememberLauncherForActivityResult] gets a result back.
 * - **Implicit** — you name an *action* (`ACTION_VIEW`, `ACTION_SEND`, `ACTION_DIAL`) and Android
 *   offers any app whose **intent-filter** matches (a browser, the share sheet, the dialer).
 *
 * [DeepLinkActivity] shows the reverse: declaring a filter so links/other apps can open *you*.
 */
class IntentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "6 · Intents") {
                    IntentsScreen()
                }
            }
        }
    }
}

@Composable
private fun IntentsScreen() {
    val context = LocalContext.current
    var result by remember { mutableStateOf<String?>(null) }

    val secondScreenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { activityResult ->
        result = activityResult.data?.getStringExtra(SecondActivity.EXTRA_RESULT)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = {
                secondScreenLauncher.launch(
                    Intent(context, SecondActivity::class.java)
                        .putExtra(SecondActivity.EXTRA_INPUT, "Hello from IntentsActivity"),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Explicit: start second screen for a result") }

        result?.let { received ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Result received", style = MaterialTheme.typography.labelMedium)
                    Text(received, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Button(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Implicit: open a website") }

        Button(
            onClick = {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Shared from the Android Basics playground")
                }
                context.startActivity(Intent.createChooser(send, "Share via"))
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Implicit: share text") }

        Button(
            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+15551234567"))) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Implicit: dial a number") }
    }
}
