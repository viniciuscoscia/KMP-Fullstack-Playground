package com.viniciuscoscia.kmpfullstackplayground.workmanager

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil3.compose.AsyncImage
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
                    workManager.getWorkInfoByIdLiveData(id = id).observeAsState().value
                }
                LaunchedEffect(workerResult?.outputData) {
                    if (workerResult?.outputData != null) {
                        val filePath = workerResult.outputData.getString(
                            PhotoCompressionWorker.KEY_RESULT_PATH
                        )
                        filePath?.let {
                            val bitmap = BitmapFactory.decodeFile(it)
                            viewModel.updateCompressedBitmap(bitmap)
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    viewModel.uncompressedUri?.let {
                        Text(text = "Uncompressed photo")
                        AsyncImage(model = it, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    viewModel.compressedBitmap?.let {
                        Text(text = "Compressed photo")
                        AsyncImage(model = it, contentDescription = null)
                    }
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
