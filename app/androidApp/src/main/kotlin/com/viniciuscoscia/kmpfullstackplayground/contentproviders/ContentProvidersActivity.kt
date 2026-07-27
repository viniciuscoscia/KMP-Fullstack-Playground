package com.viniciuscoscia.kmpfullstackplayground.contentproviders

import android.content.ContentValues
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.viniciuscoscia.kmpfullstackplayground.common.DemoScaffold
import com.viniciuscoscia.kmpfullstackplayground.common.PlaygroundTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Topic #11 — Content Providers.
 *
 * A [ContentProvider][android.content.ContentProvider] is Android's standard, URI-addressed API
 * for data. The client never calls the provider directly: it asks a
 * [android.content.ContentResolver], which locates the provider by authority and crosses a process
 * boundary when necessary. `ContactsProvider` is deliberately in-process for this interactive
 * example, but the same resolver calls work with Contacts, MediaStore, and DocumentsProvider.
 *
 * The public contract is the important boundary: authority + paths + column names. A provider can
 * replace its database, files, or network cache without changing clients as long as that contract
 * remains stable.
 */
class ContentProvidersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaygroundTheme {
                DemoScaffold(title = "11 · Content Providers") {
                    ContentProvidersScreen()
                }
            }
        }
    }
}

private data class ProviderContact(
    val id: Long,
    val name: String,
    val phone: String,
)

@Composable
private fun ContentProvidersScreen() {
    val resolver = androidx.compose.ui.platform.LocalContext.current.contentResolver
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf(emptyList<ProviderContact>()) }
    var operation by remember { mutableStateOf("Query the collection to inspect the provider data.") }

    fun refresh() {
        scope.launch {
            contacts = withContext(Dispatchers.IO) { resolver.readContacts() }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProviderCard(
            title = "The request path",
            body = "ContentResolver → content://authority/path → ContentProvider → Cursor. " +
                "The provider owns access control and storage; a client only knows the contract.",
        ) {
            CodeLine("collection  ${ContactsContract.CONTACTS_URI}")
            CodeLine("one item    ${ContactsContract.contactUri(1)}")
        }

        ProviderCard(
            title = "Query through ContentResolver",
            body = "query() returns a Cursor. Read only the columns you need (the projection), " +
                "then always close it with use {}.",
        ) {
            Button(onClick = { refresh() }, modifier = Modifier.fillMaxWidth()) {
                Text("Query contacts")
            }
            contacts.forEach { contact ->
                CodeLine("#${contact.id}  ${contact.name} · ${contact.phone}")
            }
        }

        ProviderCard(
            title = "Write through the same contract",
            body = "insert(), update(), and delete() use ContentValues and return a URI or affected " +
                "row count. The provider calls notifyChange() so observers can refresh.",
        ) {
            Button(
                onClick = {
                    scope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            resolver.insert(
                                ContactsContract.CONTACTS_URI,
                                ContentValues().apply {
                                    put(ContactsContract.COLUMN_NAME, "New contact")
                                    put(ContactsContract.COLUMN_PHONE, "+55 11 99999-0000")
                                },
                            )
                        }
                        operation = "insert() returned $uri"
                        refresh()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Insert a contact") }

            Button(
                onClick = {
                    val first = contacts.firstOrNull() ?: return@Button
                    scope.launch {
                        val updated = withContext(Dispatchers.IO) {
                            resolver.update(
                                ContactsContract.contactUri(first.id),
                                ContentValues().apply {
                                    put(ContactsContract.COLUMN_NAME, "${first.name} (updated)")
                                },
                                null,
                                null,
                            )
                        }
                        operation = "update() changed $updated row(s)"
                        refresh()
                    }
                },
                enabled = contacts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Update the first contact") }

            Button(
                onClick = {
                    val last = contacts.lastOrNull() ?: return@Button
                    scope.launch {
                        val deleted = withContext(Dispatchers.IO) {
                            resolver.delete(ContactsContract.contactUri(last.id), null, null)
                        }
                        operation = "delete() removed $deleted row(s)"
                        refresh()
                    }
                },
                enabled = contacts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Delete the last contact") }
            Text(operation, style = MaterialTheme.typography.bodySmall)
        }

        ProviderCard(
            title = "Security and real-world use",
            body = "This sample is android:exported=\"false\", so only this app can call it. An " +
                "exported provider must enforce read/write permissions or per-URI grants, validate " +
                "every URI and selection, and expose the minimum data necessary. Most apps consume " +
                "system providers; create one only when another app genuinely needs your data API.",
        )
    }
}

private fun android.content.ContentResolver.readContacts(): List<ProviderContact> {
    val projection = arrayOf(
        ContactsContract.COLUMN_ID,
        ContactsContract.COLUMN_NAME,
        ContactsContract.COLUMN_PHONE,
    )
    return query(ContactsContract.CONTACTS_URI, projection, null, null, null)?.use { cursor ->
        buildList {
            val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.COLUMN_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.COLUMN_NAME)
            val phoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.COLUMN_PHONE)
            while (cursor.moveToNext()) {
                add(ProviderContact(cursor.getLong(idIndex), cursor.getString(nameIndex), cursor.getString(phoneIndex)))
            }
        }
    }.orEmpty()
}

@Composable
private fun ProviderCard(
    title: String,
    body: String,
    content: @Composable () -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun CodeLine(value: String) {
    Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
}
