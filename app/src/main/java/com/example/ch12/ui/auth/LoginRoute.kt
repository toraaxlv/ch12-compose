package com.example.ch12.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current          // Activity: dibutuhkan Credential Manager
    val scope = rememberCoroutineScope()

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onGoogleSignIn = {
            val webClientId = findWebClientId(context)
            if (webClientId == null) {
                viewModel.onGoogleSignInFailed(
                    "Google Sign-In belum dikonfigurasi. Aktifkan provider Google di " +
                        "Firebase Console, lalu unduh ulang google-services.json."
                )
            } else scope.launch {
                try {
                    viewModel.onGoogleIdToken(requestGoogleIdToken(context, webClientId))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    viewModel.onGoogleSignInFailed(googleSignInErrorMessage(e))
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister
    )
}
