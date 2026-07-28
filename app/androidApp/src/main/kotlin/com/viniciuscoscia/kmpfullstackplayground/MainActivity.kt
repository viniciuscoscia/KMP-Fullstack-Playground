package com.viniciuscoscia.kmpfullstackplayground

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.BasicsTopic
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

/**
 * Launcher / table of contents for the Android showcase, one section per PL-Coding course.
 *
 * Instead of bundling every demo into one screen, each topic lives in its own Activity. This screen
 * lists them and starts the chosen one with an **explicit Intent** — `Intent(this, X::class.java)` —
 * which is itself the core lesson of the Activities (#1), Back Stack (#2) and Intents (#6) videos.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlaygroundTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TopicList(
                        topics = BasicsTopic.all,
                        onTopicClick = { topic -> startActivity(Intent(this, topic.activityClass)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicList(topics: List<BasicsTopic>, onTopicClick: (BasicsTopic) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // `groupBy` preserves encounter order, and BasicsTopic.all is ordered by course, so the
        // sections come out in the enum's declaration order without an extra sort.
        topics.groupBy { it.course }.forEach { (course, courseTopics) ->
            item(key = course.name) {
                Column {
                    Text(
                        course.label,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(course.tagline, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.size(8.dp))
                }
            }
            items(courseTopics) { topic ->
                TopicCard(topic = topic, onClick = { onTopicClick(topic) })
            }
        }
    }
}

@Composable
private fun TopicCard(topic: BasicsTopic, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${topic.number}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(topic.title, style = MaterialTheme.typography.titleMedium)
                Text(topic.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            if (topic.status == BasicsTopic.Status.TEMPLATE) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "TEMPLATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
