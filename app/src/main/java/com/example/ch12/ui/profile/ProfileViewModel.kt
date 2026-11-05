package com.example.ch12.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch12.KEY_CAPTURED_PHOTO
import com.example.ch12.data.photo.ImageCompressor
import com.example.ch12.data.photo.PhotoStorage
import com.example.ch12.data.photo.UploadEvent
import com.example.ch12.data.profile.ProfileRepository
import com.example.ch12.util.Clock
import com.example.ch12.util.decodedSizeBytes
import com.example.ch12.util.formatBytes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Disimpan di SavedStateHandle: preview tidak hilang saat rotasi
// atau saat proses dibunuh sistem ketika aplikasi kamera/galeri terbuka.
internal const val KEY_SELECTED_PHOTO = "selectedPhoto"

data class ProfileUiState(
    val photoUrl: String? = null,           // foto yang sudah tersimpan di cloud
    val photoUpdatedAt: Long = 0L,
    val selectedPhoto: String? = null,      // URI lokal yang belum diupload (preview)
    val isCompressing: Boolean = false,
    val uploadProgress: Float? = null,      // null = tidak sedang upload; 0f..1f
    val compressionInfo: String? = null,    // "4000×3000 (45,8 MB di memori) → 400×400, 38 KB"
    val errorMessage: String? = null,
    val message: String? = null             // pesan sekali tampil (Snackbar)
) {
    val isBusy: Boolean get() = isCompressing || uploadProgress != null
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val photoStorage: PhotoStorage,
    private val imageCompressor: ImageCompressor,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(selectedPhoto = savedStateHandle[KEY_SELECTED_PHOTO])
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var uploadJob: Job? = null

    init {
        profileRepository.observeProfile()
            .onEach { profile ->
                _uiState.update {
                    it.copy(photoUrl = profile.photoUrl, photoUpdatedAt = profile.photoUpdatedAt)
                }
            }
            .launchIn(viewModelScope)

        // Hasil dari layar kamera dikirim lewat SavedStateHandle milik entri ini
        savedStateHandle.getStateFlow<String?>(KEY_CAPTURED_PHOTO, null)
            .filterNotNull()
            .onEach { uri ->
                savedStateHandle[KEY_CAPTURED_PHOTO] = null     // konsumsi sekali
                onPhotoSelected(uri)
            }
            .launchIn(viewModelScope)
    }

    // Dari Photo Picker maupun kamera: cukup tampilkan preview dulu.
    // Upload baru terjadi setelah pengguna menekan "Unggah".
    fun onPhotoSelected(uri: String) {
        // TODO [T2.2] Tampilkan preview (BELUM upload):
        //   1. Abaikan jika _uiState.value.isBusy (sedang kompres/upload).
        //   2. savedStateHandle[KEY_SELECTED_PHOTO] = uri   -> tahan process death
        //   3. _uiState.update { it.copy(selectedPhoto = uri, compressionInfo = null, errorMessage = null) }
    }

    fun cancelSelection() {
        if (_uiState.value.isBusy) return
        savedStateHandle[KEY_SELECTED_PHOTO] = null
        _uiState.update { it.copy(selectedPhoto = null, compressionInfo = null, errorMessage = null) }
    }

    fun upload() {
        val uri = _uiState.value.selectedPhoto ?: return
        if (_uiState.value.isBusy) return                 // cegah klik ganda

        uploadJob = viewModelScope.launch {
            _uiState.update { it.copy(isCompressing = true, errorMessage = null) }
            try {
                // TODO [T4.4] Lengkapi tiga langkah ini:
                //   1. val image = imageCompressor.compress(uri)
                //      -> isCompressing = false, uploadProgress = 0f,
                //         compressionInfo = compressionSummary(image.originalWidth, ..., image.bytes.size.toLong())
                //   2. photoStorage.uploadProfilePhoto(image.bytes).collect { event -> when (event) {
                //        is UploadEvent.Progress -> uploadProgress = event.fraction
                //        is UploadEvent.Done     -> langkah 3 } }
                //   3. val now = clock.now()
                //      profileRepository.updatePhotoUrl(event.downloadUrl, now).getOrThrow()
                //      savedStateHandle[KEY_SELECTED_PHOTO] = null
                //      -> photoUrl = event.downloadUrl, photoUpdatedAt = now, selectedPhoto = null,
                //         uploadProgress = null, message = "Foto profil diperbarui"
                throw NotImplementedError("Belum diimplementasikan: TODO [T4.4]")
            } catch (e: CancellationException) {
                _uiState.update { it.copy(isCompressing = false, uploadProgress = null) }
                throw e
            } catch (e: Throwable) {
                // Preview tetap ada, sehingga pengguna bisa langsung "Coba lagi"
                _uiState.update {
                    it.copy(
                        isCompressing = false,
                        uploadProgress = null,
                        errorMessage = e.message ?: "Upload gagal. Coba lagi."
                    )
                }
            }
        }
    }

    fun cancelUpload() {
        uploadJob?.cancel()
        uploadJob = null
    }

    // Degradasi bertahap: kamera ditolak bukan jalan buntu
    fun onCameraPermissionDenied() {
        _uiState.update {
            it.copy(message = "Izin kamera ditolak. Kamu tetap bisa memilih foto dari galeri.")
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }
}

internal fun compressionSummary(
    originalWidth: Int, originalHeight: Int,
    width: Int, height: Int, sizeBytes: Long
): String {
    val decoded = decodedSizeBytes(originalWidth, originalHeight)
    return "${originalWidth}×$originalHeight (${formatBytes(decoded)} di memori) → " +
        "${width}×$height, ${formatBytes(sizeBytes)}"
}
