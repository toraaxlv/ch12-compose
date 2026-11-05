package com.example.ch12.data

import com.example.ch12.data.auth.authErrorMessage
import com.example.ch12.data.auth.authErrorMessageForCode
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorsTest {

    // Pesan yang sama untuk ketiganya: tidak membocorkan apakah email terdaftar
    @Test fun `kredensial salah tidak membedakan email dan password`() {
        val expected = "Email atau password salah."
        listOf("ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND").forEach {
            assertEquals(it, expected, authErrorMessageForCode(it))
        }
    }

    @Test fun `email sudah terdaftar`() =
        assertEquals("Email sudah terdaftar. Silakan masuk.", authErrorMessageForCode("ERROR_EMAIL_ALREADY_IN_USE"))

    @Test fun `provider belum diaktifkan menunjuk ke Firebase Console`() =
        assertEquals(true, authErrorMessageForCode("ERROR_OPERATION_NOT_ALLOWED").contains("Firebase Console"))

    @Test fun `kode tidak dikenal memakai pesan umum`() =
        assertEquals("Autentikasi gagal. Coba lagi.", authErrorMessageForCode("ERROR_BARU"))

    // Pesan asli dari server saat google-services.json masih placeholder
    @Test fun `API key tidak valid menunjuk ke google-services json`() = assertEquals(
        true,
        authErrorMessage(Exception("An internal error has occurred. [ API key not valid. Please pass a valid API key. ]"))
            .contains("google-services.json")
    )

    @Test fun `exception biasa memakai pesannya sendiri`() =
        assertEquals("boom", authErrorMessage(IllegalStateException("boom")))
}
