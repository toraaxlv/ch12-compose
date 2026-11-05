package com.example.ch12.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TagsTest {
    @Test fun `dipisah koma, dirapikan, huruf kecil, tanpa duplikat`() =
        assertEquals(listOf("kuliah", "ide"), parseTags("Kuliah, #Ide,  kuliah ,,"))

    @Test fun `teks kosong menghasilkan daftar kosong`() =
        assertEquals(emptyList<String>(), parseTags("  , ,"))

    @Test fun `format kembali ke teks`() =
        assertEquals("kuliah, ide", formatTags(listOf("kuliah", "ide")))
}
