package com.example.ch12.ui.edit

import androidx.lifecycle.SavedStateHandle
import com.example.ch12.ARG_NOTE_ID
import com.example.ch12.NEW_NOTE_ID
import com.example.ch12.data.model.Note
import com.example.ch12.testutil.FakeNoteRepository
import com.example.ch12.testutil.MainDispatcherRule
import com.example.ch12.util.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    // Argumen route disimulasikan lewat SavedStateHandle, sama seperti Navigation
    private fun viewModel(repo: FakeNoteRepository, noteId: String = NEW_NOTE_ID, now: Long = 5_000L) =
        NoteEditViewModel(repo, Clock { now }, SavedStateHandle(mapOf(ARG_NOTE_ID to noteId)))

    @Test fun `judul kosong ditolak`() = runTest {
        val repo = FakeNoteRepository()
        val vm = viewModel(repo)
        vm.onTitleChange("  ")
        vm.save()
        assertEquals("Judul tidak boleh kosong", vm.uiState.value.errorMessage)
        assertTrue(repo.current.isEmpty())
    }

    // save() tidak menunggu server: isSaved langsung true, tanpa runCurrent()
    @Test fun `catatan baru tersimpan seketika dengan waktu dari clock`() = runTest {
        val repo = FakeNoteRepository()
        val vm = viewModel(repo)
        assertTrue(vm.isNewNote)

        vm.onTitleChange(" Rencana ")
        vm.onTagsChange("Kuliah, #ide")
        vm.save()

        assertTrue(vm.uiState.value.isSaved)
        val saved = repo.current.single()
        assertEquals("Rencana", saved.title)
        assertEquals(listOf("kuliah", "ide"), saved.tags)
        assertEquals(5_000L, saved.createdAt)
    }

    @Test fun `edit mempertahankan id, createdAt, dan isPinned`() = runTest {
        val existing = Note("doc-1", "Lama", "isi", createdAt = 1_000L, updatedAt = 1_000L,
            isPinned = true, tags = listOf("a"))
        val repo = FakeNoteRepository(listOf(existing))
        val vm = viewModel(repo, noteId = "doc-1", now = 9_000L)
        runCurrent()
        assertEquals("Lama", vm.uiState.value.title)
        assertFalse(vm.uiState.value.isLoading)

        vm.onTitleChange("Baru")
        vm.save()

        val saved = repo.current.single()
        assertEquals("doc-1", saved.id)
        assertEquals("Baru", saved.title)
        assertEquals(1_000L, saved.createdAt)
        assertTrue(saved.isPinned)
        assertEquals(9_000L, saved.updatedAt)
    }

    @Test fun `catatan tidak ditemukan tidak bisa disimpan`() = runTest {
        val repo = FakeNoteRepository()
        val vm = viewModel(repo, noteId = "hilang")
        runCurrent()
        assertEquals("Catatan tidak ditemukan.", vm.uiState.value.errorMessage)

        vm.onTitleChange("Coba")
        vm.save()
        assertFalse(vm.uiState.value.isSaved)
        assertTrue(repo.current.isEmpty())
    }

    @Test fun `gagal menulis menampilkan pesan`() = runTest {
        val repo = FakeNoteRepository().apply { writeError = IllegalStateException("Anda belum login.") }
        val vm = viewModel(repo)
        vm.onTitleChange("A")
        vm.save()
        assertEquals("Anda belum login.", vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isSaved)
    }
}
