package com.viniciuscoscia.kmpfullstackplayground.intents

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import com.viniciuscoscia.kmpfullstackplayground.common.findActivity

/** The target of the explicit intent in [IntentsActivity]; reads an extra and returns a result. */
class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val input = intent.getStringExtra(EXTRA_INPUT) ?: "(no input)"
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "Second screen") {
                    SecondScreen(input = input)
                }
            }
        }
    }

    companion object {
        const val EXTRA_INPUT = "extra_input"
        const val EXTRA_RESULT = "extra_result"
    }
}

@Composable
private fun SecondScreen(input: String) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Received extra:", style = MaterialTheme.typography.labelMedium)
        Text(input, style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = {
                val activity = context.findActivity() ?: return@Button
                activity.setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(SecondActivity.EXTRA_RESULT, "Answer from the second screen"),
                )
                activity.finish()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Return a result") }
    }
}
