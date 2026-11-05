package com.example.ch12.ui.auth

import com.example.ch12.testutil.FakeAuthRepository
import com.example.ch12.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository(accounts = mapOf("ani@kampus.id" to "rahasia"))
    private val vm = LoginViewModel(auth)

    @Test fun `field kosong ditolak tanpa memanggil server`() = runTest {
        vm.login(); runCurrent()
        assertEquals("Email dan password wajib diisi.", vm.uiState.value.errorMessage)
        assertEquals(0, auth.loginCalls)
    }

    @Test fun `login berhasil mengubah status login, email di-trim`() = runTest {
        vm.onEmailChange("  ani@kampus.id ")
        vm.onPasswordChange("rahasia")
        vm.login(); runCurrent()

        assertEquals("ani@kampus.id", auth.currentUser?.email)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun `password salah menampilkan pesan dan tetap belum login`() = runTest {
        vm.onEmailChange("ani@kampus.id")
        vm.onPasswordChange("salah")
        vm.login(); runCurrent()

        assertEquals("Email atau password salah.", vm.uiState.value.errorMessage)
        assertNull(auth.currentUser)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test fun `mengetik ulang menghapus pesan error`() = runTest {
        vm.login(); runCurrent()
        vm.onPasswordChange("r")
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun `token Google diteruskan ke repository`() = runTest {
        vm.onGoogleIdToken("token-dari-google"); runCurrent()
        assertEquals("token-dari-google", auth.lastGoogleToken)
        assertEquals("google@example.com", auth.currentUser?.email)
    }

    @Test fun `dialog Google dibatalkan bukan error`() = runTest {
        vm.onGoogleSignInFailed(null)
        assertNull(vm.uiState.value.errorMessage)

        vm.onGoogleSignInFailed("Tidak ada akun Google di perangkat ini.")
        assertEquals("Tidak ada akun Google di perangkat ini.", vm.uiState.value.errorMessage)
    }
}
