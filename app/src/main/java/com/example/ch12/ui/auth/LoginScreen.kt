package com.example.ch12.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Screen murni: hanya state dan callback
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CatatanKu Cloud", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Catatanmu tersimpan di cloud dan sinkron di semua perangkat",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        EmailField(uiState.email, onEmailChange, enabled = !uiState.isLoading)
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "Password",
            enabled = !uiState.isLoading,
            onDone = onLogin
        )

        AuthError(uiState.errorMessage)

        Spacer(Modifier.height(24.dp))
        PrimaryAuthButton("Masuk", uiState.isLoading, onLogin)

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Masuk dengan Google")
        }

        TextButton(onClick = onNavigateToRegister, enabled = !uiState.isLoading) {
            Text("Belum punya akun? Daftar")
        }
    }
}
