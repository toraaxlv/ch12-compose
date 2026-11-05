package com.example.ch12.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ch12.ui.camera.hasCameraHardware
import com.example.ch12.ui.camera.openAppSettings

enum class PermissionDialog { Rationale, Settings }

// Route = tempat semua yang butuh Activity: launcher hasil, izin, Pengaturan.
// Screen tetap presentasional dan bisa di-preview.
@Composable
fun ProfileRoute(
    onNavigateBack: () -> Unit,
    onOpenCamera: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hasCamera = remember { context.hasCameraHardware() }
    // rememberSaveable: dialog tetap terbuka setelah rotasi layar
    var dialog by rememberSaveable { mutableStateOf<PermissionDialog?>(null) }

    // TODO [T2.1] Daftarkan Photo Picker (delegasi, TANPA izin):
    //   val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
    //       if (uri != null) viewModel.onPhotoSelected(uri.toString())    // null = dibatalkan
    //   }
    //   lalu ganti isi launchPicker dengan:
    //   pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    val launchPicker: () -> Unit = {}

    // TODO [T3.3] Daftarkan launcher izin kamera:
    //   val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    //       when (permissionResultAction(isGranted, context.shouldShowCameraRationale())) {
    //           PermissionAction.OpenCamera   -> onOpenCamera()
    //           PermissionAction.ShowDenied   -> viewModel.onCameraPermissionDenied()
    //           PermissionAction.OpenSettings -> dialog = PermissionDialog.Settings
    //           else -> Unit
    //       }
    //   }
    //   lalu ganti isi requestCameraPermission dengan: requestCamera.launch(Manifest.permission.CAMERA)
    val requestCameraPermission: () -> Unit = {}

    ProfileScreen(
        uiState = uiState,
        hasCamera = hasCamera,
        permissionDialog = dialog,
        onNavigateBack = onNavigateBack,
        onPickFromGallery = launchPicker,
        // Izin diminta DALAM KONTEKS: saat tombol Kamera diketuk
        // TODO [T3.3] Izin diminta DALAM KONTEKS: saat tombol Kamera diketuk.
        //   when (cameraClickAction(context.hasCameraPermission(), context.shouldShowCameraRationale())) {
        //       OpenCamera -> onOpenCamera(); ShowRationale -> dialog = PermissionDialog.Rationale
        //       RequestPermission -> requestCameraPermission(); else -> Unit }
        //   JANGAN langsung onOpenCamera(): izin bisa dicabut kapan saja.
        onTakePhoto = { onOpenCamera() },
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
