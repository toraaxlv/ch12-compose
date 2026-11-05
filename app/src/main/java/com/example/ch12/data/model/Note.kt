package com.example.ch12.data.model

// Model domain. ID berupa String karena dibuat oleh Firestore, bukan
// autoGenerate Int seperti di Room (Chapter 7).
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = createdAt,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList(),
    // Hanya ada di perangkat, TIDAK disimpan ke Firestore: true selama
    // perubahan masih di cache lokal dan belum dikonfirmasi server.
    val hasPendingWrites: Boolean = false
)
