package com.example.ch12.ui.session

import com.example.ch12.data.model.AuthUser
import com.example.ch12.data.model.UserProfile
import com.example.ch12.testutil.FakeAuthRepository
import com.example.ch12.testutil.FakeProfileRepository
import com.example.ch12.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val ani = AuthUser(uid = "uid-ani", email = "ani@kampus.id")

    // Pengguna yang sudah login sebelumnya tidak melihat layar login lagi
    @Test fun `nilai awal langsung dari sesi tersimpan`() {
        val vm = SessionViewModel(FakeAuthRepository(initialUser = ani), FakeProfileRepository())
        assertEquals(ani, vm.currentUser.value)
    }

    @Test fun `login dan logout mengubah state`() = runTest {
        val auth = FakeAuthRepository(accounts = mapOf("ani@kampus.id" to "rahasia"))
        val vm = SessionViewModel(auth, FakeProfileRepository())
        assertNull(vm.currentUser.value)

        auth.login("ani@kampus.id", "rahasia"); runCurrent()
        assertEquals("ani@kampus.id", vm.currentUser.value?.email)

        vm.logout(); runCurrent()
        assertNull(vm.currentUser.value)
    }

    @Test fun `foto profil tersedia untuk avatar`() = runTest {
        val profiles = FakeProfileRepository(UserProfile("https://x/ani.jpg", 1L))
        val vm = SessionViewModel(FakeAuthRepository(initialUser = ani), profiles)
        backgroundScope.launch { vm.profile.collect {} }   // WhileSubscribed: butuh pengamat
        runCurrent()
        assertEquals("https://x/ani.jpg", vm.profile.value.photoUrl)
    }
}
