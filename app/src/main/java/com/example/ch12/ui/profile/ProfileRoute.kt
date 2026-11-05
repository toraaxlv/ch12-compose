package com.example.ch12.ui.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ch12.ui.camera.hasCameraHardware
import com.example.ch12.ui.camera.hasCameraPermission
import com.example.ch12.ui.camera.openAppSettings
import com.example.ch12.ui.camera.shouldShowCameraRationale
import com.example.ch12.util.PermissionAction
import com.example.ch12.util.cameraClickAction
import com.example.ch12.util.permissionResultAction

enum class PermissionDialog { Rationale, Settings }

// Route = tempat semua yang butuh Activity: launcher hasil, izin, Pengaturan.
// Screen tetap presentasional dan bisa di-preview.
@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onOpenCamera: () -> Unit,
    capturedPhoto: String? = null,
    onCapturedPhotoConsumed: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hasil kamera tiba setelah layar ini kembali tampil: tampilkan sebagai preview.
    LaunchedEffect(capturedPhoto) {
        if (capturedPhoto != null) {
            viewModel.onPhotoSelected(capturedPhoto)
            onCapturedPhotoConsumed()
        }
    }
    val context = LocalContext.current
    val hasCamera = remember { context.hasCameraHardware() }
    // rememberSaveable: dialog tetap terbuka setelah rotasi layar
    var dialog by rememberSaveable { mutableStateOf<PermissionDialog?>(null) }

    // Photo Picker = delegasi: sistem yang menampilkan galeri dan memberi
    // URI sementara. Aplikasi tidak pernah memegang izin baca media.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoSelected(uri.toString())    // null = dibatalkan
    }
    val launchPicker: () -> Unit = {
        pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        when (permissionResultAction(isGranted, context.shouldShowCameraRationale())) {
            PermissionAction.OpenCamera -> onOpenCamera()
            PermissionAction.ShowDenied -> viewModel.onCameraPermissionDenied()
            PermissionAction.OpenSettings -> dialog = PermissionDialog.Settings
            else -> Unit
        }
    }
    val requestCameraPermission: () -> Unit = { requestCamera.launch(Manifest.permission.CAMERA) }

    ProfileScreen(
        uiState = uiState,
        hasCamera = hasCamera,
        permissionDialog = dialog,
        onNavigateBack = onNavigateBack,
        onPickFromGallery = launchPicker,
        // Izin diminta DALAM KONTEKS: saat tombol Kamera diketuk, bukan saat app dibuka.
        // Jangan langsung onOpenCamera(): izin bisa dicabut kapan saja dari Pengaturan.
        onTakePhoto = {
            when (cameraClickAction(context.hasCameraPermission(), context.shouldShowCameraRationale())) {
                PermissionAction.OpenCamera -> onOpenCamera()
                PermissionAction.ShowRationale -> dialog = PermissionDialog.Rationale
                PermissionAction.RequestPermission -> requestCameraPermission()
                else -> Unit
            }
        },
        onRationaleAccepted = {
            dialog = null
            requestCameraPermission()
        },
        onRationaleDeclined = {
            dialog = null
            launchPicker()
        },
        onOpenSettings = {
            dialog = null
            context.openAppSettings()
        },
        onDismissDialog = { dialog = null },
        onUpload = viewModel::upload,
        onCancelSelection = viewModel::cancelSelection,
        onCancelUpload = viewModel::cancelUpload,
        onMessageShown = viewModel::onMessageShown
    )
}
