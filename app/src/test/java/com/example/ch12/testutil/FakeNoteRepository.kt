package com.example.ch12.testutil

import com.example.ch12.data.model.Note
import com.example.ch12.data.note.NoteRepository
import com.example.ch12.data.note.sortNotes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// Pengganti Firestore: Map id -> Note dalam memori, dengan Flow seperti snapshot listener
class FakeNoteRepository(initial: List<Note> = emptyList()) : NoteRepository {

    private val notes = MutableStateFlow(initial.associateBy { it.id })
    private var nextId = 1

    // Jika diisi, observeNotes() gagal seperti listener yang ditolak Security Rules
    var observeError: Exception? = null
    // Jika diisi, semua penulisan gagal (mis. belum login)
    var writeError: Exception? = null

    val current: List<Note> get() = sortNotes(notes.value.values.toList())

    override fun observeNotes(): Flow<List<Note>> {
        observeError?.let { e -> return flow { throw e } }
        return notes.map { sortNotes(it.values.toList()) }
    }

    override suspend fun getNote(id: String): Note? = notes.value[id]

    override fun addNote(note: Note): Result<String> = write {
        val id = "note-${nextId++}"
        notes.update { it + (id to note.copy(id = id)) }
        id
    }

    override fun updateNote(note: Note): Result<Unit> = write {
        notes.update { it + (note.id to note) }
    }

    override fun deleteNote(id: String): Result<Unit> = write {
        notes.update { it - id }
    }

    private fun <T> write(block: () -> T): Result<T> =
        writeError?.let { Result.failure(it) } ?: Result.success(block())
}
