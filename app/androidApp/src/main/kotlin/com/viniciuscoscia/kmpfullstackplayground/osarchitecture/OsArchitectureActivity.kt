package com.viniciuscoscia.kmpfullstackplayground.osarchitecture

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android Internals section 2 — Android OS System Architecture. Covers two lessons so far:
 *
 * **Main Thread, Looper & MessageQueue** — mirrors Philipp Lackner's reference repo
 * (github.com/philipplackner/AndroidInternals, branch `system-architecture/thread-looper`) as
 * closely as this app's shell allows: [DemoScaffold]/[PlaygroundTheme] replace his raw
 * `Scaffold`/`AndroidInternalsTheme`, but the demonstrated behavior — variable names, control flow —
 * matches his reference `MainActivity.kt`.
 *
 * 1. **The main thread** draws the UI and dispatches input events; blocking it delays the next
 *    frame.
 * 2. **A looper keeps a thread alive.** [MyLooper] enqueues 5 runnables immediately, then a 6th
 *    after a 10s delay — watch Logcat: the background thread stays alive and drains them in FIFO
 *    order instead of dying after the first.
 * 3. **`Dispatchers.Main.immediate`** runs a block in the current call stack instead of posting to
 *    the queue. The counter button demonstrates the gotcha: `counter` inside the `onClick` lambda is
 *    the value from the *last composition*, so the printed value lags one click behind what's on
 *    screen, even though [MyViewModel]'s state itself updated immediately.
 *
 * **Handlers** — a [Handler] is a `Looper`/`MessageQueue` wrapped in a convenience API for posting
 * work onto a specific thread. The classic use case: a background thread finishes work (here, a
 * simulated API call) and needs to update UI, which is only safe from the main thread —
 * `Handler(Looper.getMainLooper()).post { ... }` schedules that update on the main looper's queue.
 */
class OsArchitectureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myLooper = MyLooper()

        repeat(5) {
            myLooper.enqueue(sampleRunnable(it))
        }
        lifecycleScope.launch {
            delay(10_000)
            myLooper.enqueue(sampleRunnable(5))
        }

        val viewModel = MyViewModel()

        val handlerResult = mutableStateOf("Waiting for the background thread…")
        Thread {
            Thread.sleep(2000L)
            val backgroundThreadName = Thread.currentThread().name
            Handler(Looper.getMainLooper()).post {
                handlerResult.value =
                    "Loaded on \"$backgroundThreadName\", UI updated on \"${Thread.currentThread().name}\""
            }
        }.start()

        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "2 · Main Thread, Looper, MessageQueue & Handlers") {
                    val counter by viewModel.counter.collectAsState(context = Dispatchers.Main.immediate)

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                viewModel.increment()
                                println("Counter: $counter")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Counter: $counter")
                        }

                        Text("Handler demo: ${handlerResult.value}")
                    }
                }
            }
        }
    }

    private fun sampleRunnable(index: Int): Runnable {
        return Runnable {
            println("Runnable $index started.")
            Thread.sleep(1000L)
            println("Runnable $index finished.")
        }
    }
}
