package com.example.ch12.data.note

import com.example.ch12.data.model.Note

// Nama field dokumen Firestore. Ditulis eksplisit (bukan toObject()) agar
// sama persis dengan yang diperiksa di firestore.rules.
const val FIELD_TITLE = "title"
const val FIELD_CONTENT = "content"
const val FIELD_CREATED_AT = "createdAt"
const val FIELD_UPDATED_AT = "updatedAt"
const val FIELD_IS_PINNED = "isPinned"
const val FIELD_TAGS = "tags"

// id tidak disimpan di dalam dokumen: id adalah nama dokumen itu sendiri.
// hasPendingWrites juga tidak: itu status cache di perangkat.
fun Note.toFirestoreMap(): Map<String, Any> = mapOf(
    FIELD_TITLE to title,
    FIELD_CONTENT to content,
    FIELD_CREATED_AT to createdAt,
    FIELD_UPDATED_AT to updatedAt,
    FIELD_IS_PINNED to isPinned,
    FIELD_TAGS to tags
)

// Skema Firestore fleksibel: dokumen lama bisa saja tidak punya field baru.
// Karena tidak ada migration seperti Room, pembaca harus memberi nilai default.
fun noteFromMap(
    id: String,
    data: Map<String, Any?>,
    hasPendingWrites: Boolean = false
): Note {
    val createdAt = (data[FIELD_CREATED_AT] as? Number)?.toLong() ?: 0L
    return Note(
        id = id,
        title = data[FIELD_TITLE] as? String ?: "",
        content = data[FIELD_CONTENT] as? String ?: "",
        createdAt = createdAt,
        updatedAt = (data[FIELD_UPDATED_AT] as? Number)?.toLong() ?: createdAt,
        isPinned = data[FIELD_IS_PINNED] as? Boolean ?: false,
        tags = (data[FIELD_TAGS] as? List<*>)?.filterIsInstance<String>().orEmpty(),
        hasPendingWrites = hasPendingWrites
    )
}

// Firestore mengurutkan berdasarkan createdAt. Catatan yang disematkan
// dipindah ke atas di perangkat: orderBy dua field butuh composite index.
fun sortNotes(notes: List<Note>) =
    notes.sortedWith(
        compareByDescending<Note> { it.isPinned }
            .thenByDescending { it.createdAt }
    )
