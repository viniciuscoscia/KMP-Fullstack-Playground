package com.viniciuscoscia.kmpfullstackplayground.viewmodels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #3 — ViewModels & Configuration Changes.
 *
 * A side-by-side comparison of three ways to hold a counter across a screen rotation:
 * - `remember` — **lost** on rotation (the composition is thrown away with the Activity).
 * - `rememberSaveable` — survives rotation (saved to the instance-state Bundle).
 * - [CounterViewModel] — survives rotation *and* process death.
 *
 * Rotate the device and see which counters keep their value.
 */
class ViewModelActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "3 · ViewModels") {
                    ViewModelScreen()
                }
            }
        }
    }
}

@Composable
private fun ViewModelScreen(viewModel: CounterViewModel = viewModel()) {
    var rememberCount by remember { mutableIntStateOf(0) }
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }
    val viewModelCount by viewModel.count.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CounterCard("remember (resets on rotation)", rememberCount, { rememberCount++ }, { rememberCount-- })
        CounterCard("rememberSaveable (survives rotation)", saveableCount, { saveableCount++ }, { saveableCount-- })
        CounterCard("ViewModel + SavedStateHandle", viewModelCount, viewModel::increment, viewModel::decrement)
    }
}

@Composable
private fun CounterCard(title: String, value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text("$value", style = MaterialTheme.typography.headlineMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDecrement) { Text("−") }
                Button(onClick = onIncrement) { Text("+") }
            }
        }
    }
}
