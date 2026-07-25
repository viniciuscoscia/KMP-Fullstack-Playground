package com.viniciuscoscia.kmpfullstackplayground.resourcesdemo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.R
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Topic #5 — Resources & Qualifiers.
 *
 * You never hard-code strings, sizes or colors — you put them in `res/` and let Android pick the
 * best match for the current device via **qualifiers** on the folder name:
 * - `values-night/` — dark mode. Toggle the system theme to change [R.string.resources_greeting].
 * - `values-de/` — German locale. Switch the phone language to German.
 * - `values-sw600dp/` — screens ≥ 600dp wide (tablets) get a larger [R.dimen.demo_card_padding].
 *
 * `pluralStringResource` even chooses the correct wording for one vs many items.
 */
class ResourcesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "5 · Resources") {
                    ResourcesScreen()
                }
            }
        }
    }
}

@Composable
private fun ResourcesScreen() {
    val configuration = LocalConfiguration.current
    val isNight = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val cardPadding = dimensionResource(R.dimen.demo_card_padding)
    var count by remember { mutableIntStateOf(1) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.resources_greeting), style = MaterialTheme.typography.titleMedium)
                Text("Night mode active: $isNight")
                Text("Card padding (sw600dp-aware): $cardPadding")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(pluralStringResource(R.plurals.apple_count, count, count), style = MaterialTheme.typography.titleMedium)
                Slider(value = count.toFloat(), onValueChange = { count = it.toInt() }, valueRange = 0f..10f, steps = 9)
            }
        }
    }
}
