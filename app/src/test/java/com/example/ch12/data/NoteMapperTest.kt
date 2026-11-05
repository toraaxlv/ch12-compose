package com.example.ch12.data

import com.example.ch12.data.model.Note
import com.example.ch12.data.note.noteFromMap
import com.example.ch12.data.note.sortNotes
import com.example.ch12.data.note.toFirestoreMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteMapperTest {

    private val note = Note(
        id = "abc", title = "Belajar Firestore", content = "isi",
        createdAt = 1_000L, updatedAt = 2_000L, isPinned = true, tags = listOf("kuliah")
    )

    @Test fun `id dan hasPendingWrites tidak ikut disimpan`() {
        val map = note.copy(hasPendingWrites = true).toFirestoreMap()
        assertEquals(setOf("title", "content", "createdAt", "updatedAt", "isPinned", "tags"), map.keys)
    }

    @Test fun `map ke Note dan kembali menghasilkan catatan yang sama`() =
        assertEquals(note, noteFromMap("abc", note.toFirestoreMap()))

    // Skema fleksibel: dokumen lama yang belum punya field baru tetap terbaca
    @Test fun `field yang tidak ada diberi nilai default`() {
        val old = noteFromMap("x", mapOf("title" to "Lama", "createdAt" to 500L))
        assertEquals("Lama", old.title)
        assertEquals("", old.content)
        assertEquals("updatedAt jatuh ke createdAt", 500L, old.updatedAt)
        assertFalse(old.isPinned)
        assertEquals(emptyList<String>(), old.tags)
    }

    @Test fun `tipe yang salah tidak membuat crash`() {
        val odd = noteFromMap("y", mapOf("title" to 42, "tags" to listOf("a", 1, null)))
        assertEquals("", odd.title)
        assertEquals(listOf("a"), odd.tags)
    }

    @Test fun `status pending writes dibawa dari metadata snapshot`() =
        assertTrue(noteFromMap("z", emptyMap(), hasPendingWrites = true).hasPendingWrites)

    @Test fun `disematkan di atas lalu terbaru dulu`() {
        val sorted = sortNotes(listOf(
            Note(id = "lama", createdAt = 1), Note(id = "baru", createdAt = 3),
            Note(id = "pin", createdAt = 0, isPinned = true)
        ))
        assertEquals(listOf("pin", "baru", "lama"), sorted.map { it.id })
    }
}
