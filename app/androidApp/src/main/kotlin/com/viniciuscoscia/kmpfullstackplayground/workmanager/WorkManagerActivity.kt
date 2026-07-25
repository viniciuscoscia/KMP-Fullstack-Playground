package com.viniciuscoscia.kmpfullstackplayground.workmanager

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme

class WorkManagerActivity : ComponentActivity() {
    private lateinit var workManager: WorkManager

    private val viewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workManager = WorkManager.getInstance(applicationContext)

        setContent {
            PlaygroundTheme {
                val workerResult = viewModel.workId?.let {id ->
                    workManager.getWorkInfoByIdLiveData(id = id).observeAsState()
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            ?: return

        viewModel.updateUncompressedUri(uri)

        val request = OneTimeWorkRequestBuilder<PhotoCompressionWorker>()
            .setInputData(
                workDataOf(
                    PhotoCompressionWorker.KEY_CONTENT_URI to uri.toString(),
                    PhotoCompressionWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 20L
                )
            )
            .setConstraints(
                Constraints(
                    requiresStorageNotLow = true
                )
            ).build()

        viewModel.updateWorkId(request.id)
        workManager.enqueue(request)
    }
}
