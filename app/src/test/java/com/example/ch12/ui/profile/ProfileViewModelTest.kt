package com.example.ch12.ui.profile

import androidx.lifecycle.SavedStateHandle
import com.example.ch12.KEY_CAPTURED_PHOTO
import com.example.ch12.data.model.UserProfile
import com.example.ch12.testutil.FakeImageCompressor
import com.example.ch12.testutil.FakePhotoStorage
import com.example.ch12.testutil.FakeProfileRepository
import com.example.ch12.testutil.MainDispatcherRule
import com.example.ch12.util.Clock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val photo = "content://media/picker/0/42"
    private val profileRepo = FakeProfileRepository()
    private val storage = FakePhotoStorage(downloadUrl = "https://storage.example/ani.jpg")
    private val compressor = FakeImageCompressor()
    private val handle = SavedStateHandle()

    private fun viewModel() =
        ProfileViewModel(profileRepo, storage, compressor, Clock { 9_000L }, handle)

    @Test fun `foto di cloud tampil dari repository`() {
        profileRepo.profile.value = UserProfile("https://storage.example/lama.jpg", 1L)
        val state = viewModel().uiState.value
        assertEquals("https://storage.example/lama.jpg", state.photoUrl)
        assertNull(state.selectedPhoto)
    }

    @Test fun `memilih foto hanya menampilkan preview, belum upload`() {
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        assertEquals(photo, vm.uiState.value.selectedPhoto)
        assertEquals(0, storage.uploadCalls)
        // Bertahan setelah process death
        assertEquals(photo, handle[KEY_SELECTED_PHOTO])
    }

    @Test fun `preview dipulihkan dari SavedStateHandle`() {
        handle[KEY_SELECTED_PHOTO] = photo
        assertEquals(photo, viewModel().uiState.value.selectedPhoto)
    }

    @Test fun `hasil kamera dari SavedStateHandle menjadi preview`() = runTest {
        val vm = viewModel()
        handle[KEY_CAPTURED_PHOTO] = "file:///cache/camera/capture_1.jpg"
        runCurrent()
        assertEquals("file:///cache/camera/capture_1.jpg", vm.uiState.value.selectedPhoto)
        assertNull(handle[KEY_CAPTURED_PHOTO])          // dikonsumsi sekali
    }

    @Test fun `upload mengompres dulu lalu menampilkan progres`() = runTest {
        storage.gate = CompletableDeferred()
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); runCurrent()

        assertEquals(listOf(photo), compressor.compressedUris)
        assertEquals(40 * 1024, storage.lastBytes?.size)  // yang diupload hasil kompresi
        val state = vm.uiState.value
        assertEquals(0.5f, state.uploadProgress!!, 0.001f)
        assertTrue(state.isBusy)
        assertEquals("4000×3000 (45,8 MB di memori) → 400×400, 40 KB", state.compressionInfo)
        storage.gate!!.complete(Unit)
    }

    @Test fun `upload berhasil menyimpan URL ke profil`() = runTest {
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); runCurrent()

        assertEquals(UserProfile("https://storage.example/ani.jpg", 9_000L), profileRepo.profile.value)
        val state = vm.uiState.value
        assertEquals("https://storage.example/ani.jpg", state.photoUrl)
        assertNull(state.selectedPhoto)
        assertNull(state.uploadProgress)
        assertEquals("Foto profil diperbarui", state.message)
        assertNull(handle[KEY_SELECTED_PHOTO])
    }

    @Test fun `upload gagal mempertahankan preview untuk coba lagi`() = runTest {
        storage.failureMessage = "Koneksi terputus."
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); runCurrent()

        val state = vm.uiState.value
        assertEquals("Koneksi terputus.", state.errorMessage)
        assertEquals(photo, state.selectedPhoto)
        assertFalse(state.isBusy)
        assertNull(profileRepo.profile.value.photoUrl)
    }

    @Test fun `gambar rusak tidak diupload`() = runTest {
        compressor.failureMessage = "Berkas yang dipilih bukan gambar."
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); runCurrent()

        assertEquals("Berkas yang dipilih bukan gambar.", vm.uiState.value.errorMessage)
        assertEquals(0, storage.uploadCalls)
    }

    @Test fun `klik ganda hanya satu upload`() = runTest {
        storage.gate = CompletableDeferred()
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); vm.upload(); runCurrent()
        assertEquals(1, storage.uploadCalls)
        storage.gate!!.complete(Unit)
    }

    @Test fun `batalkan upload menghentikan proses`() = runTest {
        storage.gate = CompletableDeferred()
        val vm = viewModel()
        vm.onPhotoSelected(photo)
        vm.upload(); runCurrent()

        vm.cancelUpload(); runCurrent()
        assertFalse(vm.uiState.value.isBusy)
        assertEquals(photo, vm.uiState.value.selectedPhoto)
        assertNull(profileRepo.profile.value.photoUrl)
    }

    @Test fun `izin kamera ditolak menawarkan galeri`() {
        val vm = viewModel()
        vm.onCameraPermissionDenied()
        assertNotNull(vm.uiState.value.message)
        assertTrue(vm.uiState.value.message!!.contains("galeri"))
    }
}
