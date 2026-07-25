package com.viniciuscoscia.kmpfullstackplayground.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Shared chrome for every topic screen: a top app bar showing the topic title with a back button
 * that finishes the hosting Activity, over a scrollable, padded content column.
 *
 * Centralising this keeps each topic Activity focused on the single Android concept it demonstrates
 * instead of repeating layout boilerplate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScaffold(
    title: String,
    content: @Composable () -> Unit,
) {
    val activity = LocalContext.current.findActivity()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            content()
        }
    }
}
