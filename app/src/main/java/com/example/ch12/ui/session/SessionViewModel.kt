package com.example.ch12.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.model.AuthUser
import com.example.ch12.data.model.UserProfile
import com.example.ch12.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// Status login adalah state seperti state lainnya (Chapter 4).
// Navigasi di CatatanKuApp menjadi fungsi dari state ini.
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    // initialValue = currentUser: tersedia segera dari disk, sehingga layar
    // pertama langsung benar tanpa "kedipan" layar login.
    val currentUser: StateFlow<AuthUser?> = authRepository.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = authRepository.currentUser
    )

    // Chapter 12: avatar di daftar catatan. Ikut berganti saat akun berganti.
    val profile: StateFlow<UserProfile> = profileRepository.observeProfile().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserProfile()
    )

    // Cukup logout. Navigasi ke layar login terjadi karena state berubah.
    fun logout() = authRepository.logout()
}
