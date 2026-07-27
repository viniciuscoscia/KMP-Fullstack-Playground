package com.viniciuscoscia.kmpfullstackplayground.uris

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Topic #10 — Uris.
 *
 * A `Uri` is **not** a file path. It is a pointer shaped like
 * `scheme://authority/path?query#fragment`, and the *scheme* decides who resolves it. Android
 * leans on three of them constantly:
 *
 * | Scheme | Points at | Who may read it |
 * |---|---|---|
 * | `android.resource://<pkg>/<type>/<name>` | something bundled in `res/` | your app |
 * | `file:///data/user/0/<pkg>/files/…` | a real path on disk | your app only — handing one to another app throws `FileUriExposedException` since API 24 |
 * | `content://<authority>/<path>` | a row served by a `ContentProvider` | whoever you grant permission to |
 *
 * The payoff is [android.content.ContentResolver]: it dispatches on the scheme, so the *same*
 * `openInputStream(uri)` call reads a bundled drawable, a private file, or another app's photo.
 * Code that takes a `Uri` instead of a `String` path works with all three for free.
 *
 * [FileProvider] is the bridge between the last two rows — it republishes one of your own files as
 * a `content://` uri that other apps can open, without ever revealing where the file really lives.
 * The directories it is allowed to serve are declared in `res/xml/file_paths.xml`.
 *
 * This is also the natural lead-in to topic #11: a `content://` uri is just an address, and a
 * `ContentProvider` is the thing sitting at that address answering queries.
 */
class UrisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "10 · Uris") {
                    UrisScreen()
                }
            }
        }
    }
}

/** The same image, addressed three different ways. */
private data class SampleUris(
    val resource: Uri,
    val file: Uri,
    val content: Uri,
    val bytes: Int,
)

/** A hand-written uri used only to show off the parsing helpers on [Uri]. */
private val ANATOMY_URI: Uri =
    "content://media/external/images/media/42?width=1080#top".toUri()

private const val SHARED_DIR = "shared_images"
private const val SAMPLE_FILE = "sample_photo.png"
private const val SAMPLE_MIME = "image/png"

@Composable
private fun UrisScreen() {
    val context = LocalContext.current
    var sample by remember { mutableStateOf<SampleUris?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedMetadata by remember { mutableStateOf<String?>(null) }
    var shareError by remember { mutableStateOf<String?>(null) }

    // Disk I/O, so it stays off the main thread; LaunchedEffect(Unit) runs it once per composition
    // and cancels with the screen.
    LaunchedEffect(Unit) {
        sample = withContext(Dispatchers.IO) { context.prepareSampleUris() }
    }

    // The photo picker hands back a content:// uri owned by *another* app, with a temporary read
    // grant attached to this Activity. No storage permission involved.
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        pickedUri = uri
        pickedMetadata = uri?.let { context.describeOpenable(it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        UriCard(
            title = "Anatomy of a Uri",
            explanation = "Uri parses the string for you — never split it by hand.",
            uri = ANATOMY_URI,
        ) {
            Field("scheme", ANATOMY_URI.scheme)
            Field("authority", ANATOMY_URI.authority)
            Field("path", ANATOMY_URI.path)
            Field("pathSegments", ANATOMY_URI.pathSegments.joinToString("/"))
            Field("lastPathSegment", ANATOMY_URI.lastPathSegment)
            Field("query \"width\"", ANATOMY_URI.getQueryParameter("width"))
            Field("fragment", ANATOMY_URI.fragment)
            // buildUpon() copies the uri so you can edit one piece — Uri itself is immutable.
            Field(
                "buildUpon()",
                ANATOMY_URI.buildUpon().appendQueryParameter("format", "png").build().toString(),
            )
        }

        val loaded = sample
        if (loaded == null) {
            Text("Preparing the sample image…", style = MaterialTheme.typography.bodyMedium)
        } else {
            UriCard(
                title = "1 · android.resource://",
                explanation = "Addresses a drawable compiled into the APK. ContentResolver read " +
                    "${loaded.bytes} bytes from it — no File, no path.",
                uri = loaded.resource,
            ) {
                AsyncImage(
                    model = loaded.resource,
                    contentDescription = "Sample photo loaded from a resource uri",
                    modifier = Modifier.size(160.dp),
                )
            }

            UriCard(
                title = "2 · file://",
                explanation = "Those bytes copied into this app's private filesDir. Real path, " +
                    "readable only by this process.",
                uri = loaded.file,
            ) {
                AsyncImage(
                    model = loaded.file,
                    contentDescription = "Sample photo loaded from a file uri",
                    modifier = Modifier.size(160.dp),
                )
                Button(
                    onClick = {
                        shareError = runCatching { context.shareImage(loaded.file) }
                            .exceptionOrNull()
                            ?.let { "${it::class.java.simpleName}: ${it.message}" }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Try to share this file:// uri") }
                shareError?.let {
                    Text(
                        "StrictMode blocked it — this is exactly why FileProvider exists:\n$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            UriCard(
                title = "3 · content://",
                explanation = "The very same file, republished by the FileProvider declared in " +
                    "the manifest. The authority is ours; the path is the alias from file_paths.xml.",
                uri = loaded.content,
            ) {
                AsyncImage(
                    model = loaded.content,
                    contentDescription = "Sample photo loaded from a content uri",
                    modifier = Modifier.size(160.dp),
                )
                Button(
                    onClick = { context.shareImage(loaded.content) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Share this content:// uri (works)") }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("4 · A content:// uri from another app", style = MaterialTheme.typography.titleSmall)
                Text(
                    "The photo picker returns a uri you cannot turn into a path. To learn anything " +
                        "about it you query the provider for columns.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Pick an image") }

                pickedUri?.let { uri ->
                    Text(uri.toString(), style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    pickedMetadata?.let { Field("OpenableColumns", it) }
                    AsyncImage(
                        model = uri,
                        contentDescription = "The picked image",
                        modifier = Modifier.size(160.dp),
                    )
                }
            }
        }
    }
}

/**
 * Produces the same image under all three schemes.
 *
 * Note that step 1 never touches `R.drawable` or a `File`: `openInputStream` resolves the
 * `android.resource://` uri, which is what makes the resolver worth using in the first place.
 */
private fun Context.prepareSampleUris(): SampleUris {
    val resourceUri = "android.resource://$packageName/drawable/sample_photo".toUri()
    val bytes = contentResolver.openInputStream(resourceUri)?.use { it.readBytes() } ?: ByteArray(0)

    val file = File(File(filesDir, SHARED_DIR).apply { mkdirs() }, SAMPLE_FILE)
    file.outputStream().use { it.write(bytes) }

    // The authority must match the one registered for the provider in AndroidManifest.xml, and
    // the file must sit inside a directory listed in res/xml/file_paths.xml — otherwise this
    // throws IllegalArgumentException.
    val contentUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

    return SampleUris(
        resource = resourceUri,
        file = file.toUri(),
        content = contentUri,
        bytes = bytes.size,
    )
}

/**
 * Asks the provider behind [uri] for the two columns every "openable" one must expose.
 *
 * This is the only honest way to get a display name or a size out of a `content://` uri — the last
 * path segment is an opaque id, not a filename.
 */
private fun Context.describeOpenable(uri: Uri): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "unknown"
            val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else -1L
            return "$name · $size bytes"
        }
    }
    return "the provider returned no metadata"
}

/**
 * `FLAG_GRANT_READ_URI_PERMISSION` is what actually lets the receiving app open the stream: the
 * grant is temporary and scoped to this one uri. Without it the other app gets a SecurityException
 * even for a perfectly valid `content://` uri.
 */
private fun Context.shareImage(uri: Uri) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = SAMPLE_MIME
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(send, "Share the sample photo"))
}

@Composable
private fun UriCard(
    title: String,
    explanation: String,
    uri: Uri,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(explanation, style = MaterialTheme.typography.bodySmall)
            Text(
                uri.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
            content()
        }
    }
}

@Composable
private fun Field(label: String, value: String?) {
    Text(
        text = "$label = ${value ?: "null"}",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
    )
}
