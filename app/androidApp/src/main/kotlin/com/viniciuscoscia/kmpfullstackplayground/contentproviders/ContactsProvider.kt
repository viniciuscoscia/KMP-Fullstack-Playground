package com.viniciuscoscia.kmpfullstackplayground.contentproviders

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

/** Public, stable API for clients of [ContactsProvider]. */
object ContactsContract {
    const val AUTHORITY = "com.viniciuscoscia.kmpfullstackplayground.contacts"
    private const val PATH_CONTACTS = "contacts"

    const val COLUMN_ID = "_id"
    const val COLUMN_NAME = "name"
    const val COLUMN_PHONE = "phone"

    val CONTACTS_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_CONTACTS")

    fun contactUri(id: Long): Uri = ContentUris.withAppendedId(CONTACTS_URI, id)
}

/**
 * A small in-memory provider focused on the protocol rather than persistence.
 *
 * `UriMatcher` distinguishes a collection URI (`/contacts`) from an item URI (`/contacts/#`).
 * Production providers commonly delegate these methods to Room, files, or a remote-backed cache.
 */
class ContactsProvider : ContentProvider() {
    private val contacts = mutableListOf(
        Contact(id = 1, name = "Ada Lovelace", phone = "+44 20 0000 0001"),
        Contact(id = 2, name = "Katherine Johnson", phone = "+1 202 000 0002"),
    )
    private var nextId = 3L

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = when (URI_MATCHER.match(uri)) {
        CONTACTS -> "vnd.android.cursor.dir/vnd.${ContactsContract.AUTHORITY}.contact"
        CONTACT_ID -> "vnd.android.cursor.item/vnd.${ContactsContract.AUTHORITY}.contact"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val rows = when (URI_MATCHER.match(uri)) {
            CONTACTS -> contacts
            CONTACT_ID -> contacts.filter { it.id == ContentUris.parseId(uri) }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val columns = projection?.map { it }?.toTypedArray() ?: ALL_COLUMNS
        return MatrixCursor(columns).apply {
            rows.forEach { contact ->
                addRow(columns.map { column -> contact.valueFor(column) }.toTypedArray())
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        require(URI_MATCHER.match(uri) == CONTACTS) { "Insert requires the contacts collection URI" }
        val name = values?.getAsString(ContactsContract.COLUMN_NAME)?.takeIf(String::isNotBlank)
            ?: throw IllegalArgumentException("A non-empty name is required")
        val contact = Contact(
            id = nextId++,
            name = name,
            phone = values.getAsString(ContactsContract.COLUMN_PHONE).orEmpty(),
        )
        contacts += contact
        val contactUri = ContactsContract.contactUri(contact.id)
        context?.contentResolver?.notifyChange(contactUri, null)
        return contactUri
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        require(URI_MATCHER.match(uri) == CONTACT_ID) { "Update requires one contact URI" }
        val index = contacts.indexOfFirst { it.id == ContentUris.parseId(uri) }
        if (index == -1) return 0

        val current = contacts[index]
        contacts[index] = current.copy(
            name = values?.getAsString(ContactsContract.COLUMN_NAME) ?: current.name,
            phone = values?.getAsString(ContactsContract.COLUMN_PHONE) ?: current.phone,
        )
        context?.contentResolver?.notifyChange(uri, null)
        return 1
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        require(URI_MATCHER.match(uri) == CONTACT_ID) { "Delete requires one contact URI" }
        val removed = contacts.removeAll { it.id == ContentUris.parseId(uri) }
        if (removed) context?.contentResolver?.notifyChange(uri, null)
        return if (removed) 1 else 0
    }

    private data class Contact(val id: Long, val name: String, val phone: String) {
        fun valueFor(column: String): Any? = when (column) {
            ContactsContract.COLUMN_ID -> id
            ContactsContract.COLUMN_NAME -> name
            ContactsContract.COLUMN_PHONE -> phone
            else -> throw IllegalArgumentException("Unknown column: $column")
        }
    }

    private companion object {
        const val CONTACTS = 1
        const val CONTACT_ID = 2
        val ALL_COLUMNS = arrayOf(
            ContactsContract.COLUMN_ID,
            ContactsContract.COLUMN_NAME,
            ContactsContract.COLUMN_PHONE,
        )
        val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(ContactsContract.AUTHORITY, "contacts", CONTACTS)
            addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACT_ID)
        }
    }
}
