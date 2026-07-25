package com.viniciuscoscia.kmpfullstackplayground.lifecycle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

private const val TAG = "LifecycleActivity"

/**
 * Topic #1 — Activities & the Activity Lifecycle.
 *
 * Every Activity moves through a fixed sequence of callbacks: `onCreate → onStart → onResume`
 * (becoming visible, then interactive) and `onPause → onStop → onDestroy` (going away). Rotating the
 * device destroys and recreates the Activity — watch the Logcat tag "LifecycleActivity" and the
 * on-screen log to see the exact order. Transient state kept only in memory is lost on recreation
 * unless saved in [onSaveInstanceState] (topic #3 shows the ViewModel alternative).
 */
class LifecycleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "1 · Lifecycle") {
                    LifecycleScreen()
                }
            }
        }
    }

    override fun onStart() { super.onStart(); log("onStart") }
    override fun onResume() { super.onResume(); log("onResume") }
    override fun onPause() { super.onPause(); log("onPause") }
    override fun onStop() { super.onStop(); log("onStop") }
    override fun onRestart() { super.onRestart(); log("onRestart") }
    override fun onDestroy() { super.onDestroy(); log("onDestroy") }

    private fun log(event: String) {
        Log.d(TAG, event)
        LifecycleLog.add(event)
    }
}

@Composable
private fun LifecycleScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Recorded callbacks", style = MaterialTheme.typography.titleMedium)
        Text(
            "Rotate the device, or press Home/Back and return, to see the order change. " +
                "Logcat tag: LifecycleActivity.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))
        LifecycleLog.events.forEachIndexed { index, event ->
            Text("${index + 1}. $event", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * A tiny process-wide log so the recorded callbacks survive the Activity recreation a rotation
 * causes (a plain `remember` list would reset). It is a [mutableStateListOf] so Compose observes
 * new entries and recomposes as later callbacks (onStart/onResume) fire. A teaching aid, not
 * production code.
 */
private object LifecycleLog {
    val events = mutableStateListOf<String>()
    fun add(event: String) { events.add(event) }
}
