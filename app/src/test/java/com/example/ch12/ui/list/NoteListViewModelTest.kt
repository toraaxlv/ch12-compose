package com.example.ch12.ui.list

import com.example.ch12.data.model.Note
import com.example.ch12.data.note.NoteAccessException
import com.example.ch12.testutil.FakeNoteRepository
import com.example.ch12.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun note(
        id: String, title: String, content: String = "", createdAt: Long = 0,
        pinned: Boolean = false, tags: List<String> = emptyList()
    ) = Note(id, title, content, createdAt = createdAt, updatedAt = createdAt,
        isPinned = pinned, tags = tags)

    private fun TestScope.start(vm: NoteListViewModel) {
        backgroundScope.launch { vm.uiState.collect { } }
        runCurrent()
    }

    @Test fun `disematkan di atas lalu terbaru`() = runTest {
        val vm = NoteListViewModel(FakeNoteRepository(listOf(
            note("a", "Lama", createdAt = 1), note("b", "Baru", createdAt = 2),
            note("c", "Pin", createdAt = 0, pinned = true)
        )))
        start(vm)
        assertEquals(listOf("Pin", "Baru", "Lama"), vm.uiState.value.notes.map { it.title })
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun `pencarian di perangkat tanpa debounce, judul atau isi`() = runTest {
        val vm = NoteListViewModel(FakeNoteRepository(listOf(
            note("1", "Belanja", "susu"), note("2", "Kuliah", "bab Firestore"), note("3", "Ide", "aplikasi")
        )))
        start(vm)

        vm.onQueryChange("FIRESTORE"); runCurrent()
        assertEquals(listOf("Kuliah"), vm.uiState.value.notes.map { it.title })
        assertTrue(vm.uiState.value.isFiltering)

        vm.onQueryChange(""); runCurrent()
        assertEquals(3, vm.uiState.value.notes.size)
    }

    @Test fun `filter tag dan ketuk tag yang sama melepas filter`() = runTest {
        val vm = NoteListViewModel(FakeNoteRepository(listOf(
            note("1", "A", tags = listOf("kuliah")), note("2", "B", tags = listOf("ide", "kuliah")),
            note("3", "C", tags = listOf("ide"))
        )))
        start(vm)
        assertEquals(listOf("ide", "kuliah"), vm.uiState.value.availableTags)

        vm.onTagSelected("kuliah"); runCurrent()
        assertEquals(setOf("A", "B"), vm.uiState.value.notes.map { it.title }.toSet())

        vm.onTagSelected("kuliah"); runCurrent()
        assertNull(vm.uiState.value.selectedTag)
        assertEquals(3, vm.uiState.value.notes.size)
    }

    @Test fun `hapus lalu batalkan mengembalikan dokumen dengan id yang sama`() = runTest {
        val target = note("n7", "Penting", createdAt = 5, pinned = true, tags = listOf("x"))
        val repo = FakeNoteRepository(listOf(target, note("n8", "Lain")))
        val vm = NoteListViewModel(repo)
        start(vm)

        vm.deleteNote(target); runCurrent()
        assertEquals(listOf("n8"), repo.current.map { it.id })
        assertEquals(target, vm.uiState.value.recentlyDeleted)

        vm.undoDelete(); runCurrent()
        assertTrue(target in repo.current)
        assertNull(vm.uiState.value.recentlyDeleted)
    }

    @Test fun `menyematkan tidak mengubah updatedAt`() = runTest {
        val n = note("1", "A", createdAt = 10)
        val repo = FakeNoteRepository(listOf(n))
        val vm = NoteListViewModel(repo)
        start(vm)

        vm.togglePin(n); runCurrent()
        assertTrue(repo.current.single().isPinned)
        assertEquals(10L, repo.current.single().updatedAt)
    }

    @Test fun `listener ditolak server menjadi pesan error`() = runTest {
        val repo = FakeNoteRepository().apply {
            observeError = NoteAccessException("Akses ditolak server. Periksa Security Rules di Firebase Console.")
        }
        val vm = NoteListViewModel(repo)
        start(vm)
        assertEquals(
            "Akses ditolak server. Periksa Security Rules di Firebase Console.",
            vm.uiState.value.errorMessage
        )
        assertTrue(vm.uiState.value.notes.isEmpty())
    }

    @Test fun `penulisan gagal tidak memunculkan snackbar batalkan`() = runTest {
        val n = note("1", "A")
        val repo = FakeNoteRepository(listOf(n)).apply { writeError = IllegalStateException("Anda belum login.") }
        val vm = NoteListViewModel(repo)
        start(vm)

        vm.deleteNote(n); runCurrent()
        assertNull(vm.uiState.value.recentlyDeleted)
        assertEquals("Anda belum login.", vm.uiState.value.errorMessage)
        assertEquals(1, repo.current.size)
    }
}
