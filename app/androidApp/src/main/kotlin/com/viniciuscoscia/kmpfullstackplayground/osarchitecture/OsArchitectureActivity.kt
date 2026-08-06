package com.viniciuscoscia.kmpfullstackplayground.osarchitecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Mirrors Philipp Lackner's AndroidInternals `system-architecture/thread-looper` branch while
 * keeping this app's catalog shell.
 */
class OsArchitectureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val myLooper = MyLooper()

        repeat(5) {
            myLooper.enqueue(sampleRunnable(it))
        }
        lifecycleScope.launch {
            delay(10_000)
            myLooper.enqueue(sampleRunnable(5))
        }

        val viewModel = MyViewModel()
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "2 · Main Thread, Looper & MessageQueue") {
                    val counter by viewModel.counter.collectAsState(context = Dispatchers.Main.immediate)

                    Button(
                        onClick = {
                            viewModel.increment()
                            println("Counter: $counter")
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(),
                    ) {
                        Text("Counter: $counter")
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
