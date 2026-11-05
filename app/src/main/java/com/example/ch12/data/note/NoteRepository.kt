package com.example.ch12.data.note

import com.example.ch12.data.model.Note
import kotlinx.coroutines.flow.Flow

// Interface yang sama dipakai ViewModel, apa pun penyimpanannya.
// Jika kelak pindah dari Firebase, cukup implementasinya yang diganti
// (mengurangi vendor lock-in).
interface NoteRepository {
    // Real-time: memancarkan daftar baru setiap ada perubahan, dari perangkat mana pun
    fun observeNotes(): Flow<List<Note>>

    suspend fun getNote(id: String): Note?

    // Fungsi tulis TIDAK menunggu server (lihat FirestoreNoteRepository).
    // Result.failure hanya untuk kegagalan yang langsung diketahui, mis. belum login.
    fun addNote(note: Note): Result<String>
    // Menimpa seluruh dokumen; dokumen yang sudah dihapus dibuat kembali (undo)
    fun updateNote(note: Note): Result<Unit>
    fun deleteNote(id: String): Result<Unit>
}

class NotSignedInException : IllegalStateException("Anda belum login.")
