package com.example.ch12.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.model.AuthUser
import com.example.ch12.util.validateLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Tidak ada isSuccess: setelah login berhasil, authState berubah dan
// CatatanKuApp yang memindahkan layar (navigasi = fungsi dari state).
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return
        validateLogin(state.email, state.password)?.let { message ->
            _uiState.update { it.copy(errorMessage = message) }
            return
        }
        authenticate { authRepository.login(state.email.trim(), state.password) }
    }

    // Tugas 5: token didapat Route dari Credential Manager (butuh Activity)
    fun onGoogleIdToken(idToken: String) =
        authenticate { authRepository.signInWithGoogle(idToken) }

    // message null = pengguna menutup dialog akun Google; bukan error
    fun onGoogleSignInFailed(message: String?) {
        if (message != null) _uiState.update { it.copy(errorMessage = message) }
    }

    private fun authenticate(block: suspend () -> Result<AuthUser>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = block()
            _uiState.update {
                it.copy(isLoading = false, errorMessage = result.exceptionOrNull()?.message)
            }
        }
    }
}
