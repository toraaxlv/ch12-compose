package com.example.ch12.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch12.ARG_NOTE_ID
import com.example.ch12.NEW_NOTE_ID
import com.example.ch12.data.model.Note
import com.example.ch12.data.note.NoteRepository
import com.example.ch12.util.Clock
import com.example.ch12.util.formatTags
import com.example.ch12.util.parseTags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditUiState(
    val title: String = "",
    val content: String = "",
    val tagsText: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle          // argumen route (Ch10)
) : ViewModel() {

    private val noteId: String = savedStateHandle.get<String>(ARG_NOTE_ID) ?: NEW_NOTE_ID
    val isNewNote: Boolean get() = noteId == NEW_NOTE_ID

    private val _uiState = MutableStateFlow(NoteEditUiState(isLoading = !isNewNote))
    val uiState: StateFlow<NoteEditUiState> = _uiState.asStateFlow()

    // Catatan asli saat mode edit, agar createdAt dan isPinned tidak tereset
    private var original: Note? = null

    init {
        if (!isNewNote) loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            val note = try {
                repository.getNote(noteId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Gagal memuat catatan.") }
                return@launch
            }
            original = note
            _uiState.update {
                if (note == null) {
                    it.copy(isLoading = false, errorMessage = "Catatan tidak ditemukan.")
                } else {
                    it.copy(
                        isLoading = false,
                        title = note.title,
                        content = note.content,
                        tagsText = formatTags(note.tags)
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) =
        _uiState.update { it.copy(title = value, errorMessage = null) }

    fun onContentChange(value: String) =
        _uiState.update { it.copy(content = value) }

    fun onTagsChange(value: String) =
        _uiState.update { it.copy(tagsText = value) }

    // Tanpa isSaving/loading: penyimpanan langsung masuk cache lokal, baik
    // online maupun offline. Sinkron ke server terjadi di latar belakang.
    fun save() {
        val current = _uiState.value
        if (current.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Judul tidak boleh kosong") }
            return
        }
        if (!isNewNote && original == null) return       // belum/gagal dimuat

        val now = clock.now()
        val title = current.title.trim()
        val content = current.content.trim()
        val tags = parseTags(current.tagsText)
        val existing = original

        val result = if (existing == null) {
            repository.addNote(
                Note(title = title, content = content, createdAt = now, updatedAt = now, tags = tags)
            )
        } else {
            repository.updateNote(
                existing.copy(title = title, content = content, tags = tags, updatedAt = now)
            )
        }

        _uiState.update {
            result.fold(
                onSuccess = { _ -> it.copy(isSaved = true) },
                onFailure = { e -> it.copy(errorMessage = e.message ?: "Gagal menyimpan.") }
            )
        }
    }
}
