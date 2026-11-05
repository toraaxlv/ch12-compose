package com.example.ch12.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch12.data.model.Note
import com.example.ch12.data.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NoteListUiState(
    val isLoading: Boolean = false,
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    // Catatan yang baru dihapus (untuk Snackbar "Batalkan")
    val recentlyDeleted: Note? = null,
    val errorMessage: String? = null
) {
    val isFiltering: Boolean
        get() = query.isNotBlank() || selectedTag != null
}

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedTag = MutableStateFlow<String?>(null)
    private val recentlyDeleted = MutableStateFlow<Note?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    // Listener real-time. Error (mis. ditolak Security Rules) menjadi pesan.
    private val allNotes = repository.observeNotes().catch { e ->
        errorMessage.value = e.message ?: "Gagal memuat catatan."
        emit(emptyList())
    }

    // Firestore tidak punya LIKE '%...%' seperti Room (Chapter 7). Karena semua
    // catatan pengguna sudah ada di memori (dan di cache), pencarian dilakukan di
    // perangkat. Tanpa query ke server, debounce juga tidak diperlukan lagi.
    val uiState: StateFlow<NoteListUiState> = combine(
        allNotes, query, selectedTag, recentlyDeleted, errorMessage
    ) { notes, query, tag, deleted, error ->
        val availableTags = notes.flatMap { it.tags }.distinct().sorted()
        // Tag yang sudah tidak dipakai catatan mana pun otomatis dilepas
        val activeTag = tag?.takeIf { it in availableTags }
        NoteListUiState(
            notes = filterNotes(notes, query, activeTag),
            query = query,
            availableTags = availableTags,
            selectedTag = activeTag,
            recentlyDeleted = deleted,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NoteListUiState(isLoading = true)
    )

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onTagSelected(tag: String?) {
        selectedTag.value = if (tag == selectedTag.value) null else tag
    }

    // Tidak ada viewModelScope.launch: penulisan Firestore tidak ditunggu.
    // Daftar langsung berubah karena listener membaca cache lokal.
    fun deleteNote(note: Note) {
        repository.deleteNote(note.id)
            .onSuccess { recentlyDeleted.value = note }
            .onFailure { errorMessage.value = it.message }
    }

    // Dokumen ditulis ulang dengan ID, createdAt, tag, dan pin yang sama
    fun undoDelete() {
        val note = recentlyDeleted.value ?: return
        recentlyDeleted.value = null
        repository.updateNote(note).onFailure { errorMessage.value = it.message }
    }

    fun onUndoDismissed() {
        recentlyDeleted.value = null
    }

    fun togglePin(note: Note) {
        repository.updateNote(note.copy(isPinned = !note.isPinned))
            .onFailure { errorMessage.value = it.message }
    }
}

internal fun filterNotes(notes: List<Note>, query: String, tag: String?): List<Note> {
    val q = query.trim()
    return notes.filter { note ->
        (q.isEmpty() || note.title.contains(q, ignoreCase = true) ||
            note.content.contains(q, ignoreCase = true)) &&
            (tag == null || tag in note.tags)
    }
}
