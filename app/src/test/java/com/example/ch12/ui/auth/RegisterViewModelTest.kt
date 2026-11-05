package com.example.ch12.ui.auth

import com.example.ch12.testutil.FakeAuthRepository
import com.example.ch12.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository(accounts = mapOf("lama@kampus.id" to "123456"))
    private val vm = RegisterViewModel(auth)

    private fun fill(email: String, password: String, confirm: String = password) {
        vm.onEmailChange(email)
        vm.onPasswordChange(password)
        vm.onConfirmPasswordChange(confirm)
    }

    @Test fun `konfirmasi password berbeda ditolak`() = runTest {
        fill("baru@kampus.id", "123456", "654321")
        vm.register(); runCurrent()
        assertEquals("Konfirmasi password tidak sama.", vm.uiState.value.errorMessage)
        assertNull(auth.currentUser)
    }

    @Test fun `registrasi berhasil langsung login`() = runTest {
        fill("baru@kampus.id", "123456")
        vm.register(); runCurrent()
        assertEquals("baru@kampus.id", auth.currentUser?.email)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test fun `email yang sudah terdaftar menampilkan pesan dari server`() = runTest {
        fill("lama@kampus.id", "123456")
        vm.register(); runCurrent()
        assertEquals("Email sudah terdaftar. Silakan masuk.", vm.uiState.value.errorMessage)
    }
}
