package com.example.ch12.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test fun `format email`() {
        assertTrue(isValidEmail("ani@kampus.ac.id"))
        assertTrue(isValidEmail("  ani@kampus.ac.id "))
        assertFalse(isValidEmail("ani"))
        assertFalse(isValidEmail("ani@kampus"))
        assertFalse(isValidEmail("ani @kampus.id"))
    }

    @Test fun `login - kosong, email salah, valid`() {
        assertEquals("Email dan password wajib diisi.", validateLogin("", "rahasia"))
        assertEquals("Format email tidak valid.", validateLogin("ani", "rahasia"))
        assertNull(validateLogin("ani@kampus.id", "x"))     // panjang password dicek server
    }

    @Test fun `registrasi - password pendek dan konfirmasi berbeda`() {
        assertEquals("Password minimal 6 karakter.", validateRegistration("a@b.id", "12345", "12345"))
        assertEquals("Konfirmasi password tidak sama.", validateRegistration("a@b.id", "123456", "123457"))
        assertNull(validateRegistration("a@b.id", "123456", "123456"))
    }
}
