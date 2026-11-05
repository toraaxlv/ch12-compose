package com.example.ch12.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    hasCamera: Boolean,
    permissionDialog: PermissionDialog?,
    onNavigateBack: () -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRationaleAccepted: () -> Unit,
    onRationaleDeclined: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissDialog: () -> Unit,
    onUpload: () -> Unit,
    onCancelSelection: () -> Unit,
    onCancelUpload: () -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foto Profil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview lokal (belum diunggah) didahulukan daripada foto di cloud
            val selected = uiState.selectedPhoto
            ProfilePhoto(
                model = selected ?: uiState.photoUrl,
                version = if (selected != null) 0L else uiState.photoUpdatedAt,
                size = 160.dp
            )

            if (selected != null) {
                UploadSection(
                    uiState = uiState,
                    onUpload = onUpload,
                    onCancelSelection = onCancelSelection,
                    onCancelUpload = onCancelUpload
                )
            } else {
                Text(
                    text = if (uiState.photoUrl == null) "Belum ada foto profil" else "Foto profil saat ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onPickFromGallery, enabled = !uiState.isBusy) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galeri")
                }
                // Perangkat tanpa kamera: tombol disembunyikan, galeri tetap ada
                if (hasCamera) {
                    OutlinedButton(onClick = onTakePhoto, enabled = !uiState.isBusy) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Kamera")
                    }
                }
            }

            Text(
                text = "Galeri memakai Photo Picker: tanpa izin, dan aplikasi hanya " +
                    "menerima foto yang kamu pilih.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }

    when (permissionDialog) {
        PermissionDialog.Rationale -> AlertDialog(
            onDismissRequest = onDismissDialog,
            icon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
            title = { Text("Izinkan akses kamera?") },
            text = {
                Text(
                    "Kamera hanya dipakai untuk mengambil foto profil saat kamu menekan " +
                        "tombol ambil foto. Tidak ada foto yang diambil diam-diam."
                )
            },
            confirmButton = { TextButton(onClick = onRationaleAccepted) { Text("Lanjutkan") } },
            dismissButton = { TextButton(onClick = onRationaleDeclined) { Text("Pakai galeri") } }
        )

        PermissionDialog.Settings -> AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Izin kamera ditolak") },
            text = {
                Text(
                    "Android tidak akan menampilkan dialog izin lagi. Aktifkan kamera di " +
                        "Pengaturan → Izin → Kamera, atau pilih foto dari galeri."
                )
            },
            confirmButton = { TextButton(onClick = onOpenSettings) { Text("Buka Pengaturan") } },
            dismissButton = { TextButton(onClick = onDismissDialog) { Text("Batal") } }
        )

        null -> Unit
    }
}

@Composable
private fun UploadSection(
    uiState: ProfileUiState,
    onUpload: () -> Unit,
    onCancelSelection: () -> Unit,
    onCancelUpload: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val progress = uiState.uploadProgress
        when {
            uiState.isCompressing -> {
                Text("Mengompres foto…", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            progress != null -> {
                Text("Mengunggah ${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }

            else -> Text(
                "Preview: belum diunggah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        uiState.compressionInfo?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (uiState.isBusy) {
                OutlinedButton(onClick = onCancelUpload) { Text("Batalkan upload") }
            } else {
                OutlinedButton(onClick = onCancelSelection) { Text("Batal") }
                Button(onClick = onUpload) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (uiState.errorMessage != null) "Coba lagi" else "Unggah")
                }
            }
        }
    }
}
