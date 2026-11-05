package com.example.ch12.ui.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

private const val TAG = "CameraScreen"

// Izin bisa dicabut kapan saja ("Hanya kali ini", Pengaturan, pencabutan
// otomatis). Jangan berasumsi: periksa lagi saat layar kamera dibuka.
@Composable
fun CameraRoute(
    onPhotoCaptured: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    if (!context.hasCameraPermission()) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }
    CameraScreen(onPhotoCaptured = onPhotoCaptured, onNavigateBack = onNavigateBack)
}

// CameraX: preview live di dalam aplikasi. Inilah alasan aplikasi butuh izin
// CAMERA; Photo Picker (galeri) tidak butuh izin sama sekali.
@Composable
fun CameraScreen(
    onPhotoCaptured: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // Foto profil: kamera depan lebih dulu
    var lensFacing by rememberSaveable { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var hasBothCameras by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dijalankan ulang setiap lensa diganti. bindToLifecycle: kamera otomatis
    // dilepas saat layar berhenti (aplikasi lain bisa memakainya).
    LaunchedEffect(lensFacing) {
        imageCapture = null
        try {
            val provider = context.awaitCameraProvider()
            val front = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
            val back = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            hasBothCameras = front && back
            if (lensFacing == CameraSelector.LENS_FACING_FRONT && !front && back) {
                lensFacing = CameraSelector.LENS_FACING_BACK
                return@LaunchedEffect
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            imageCapture = capture
            errorMessage = null
        } catch (e: Exception) {
            Log.e(TAG, "Kamera gagal dibuka", e)
            errorMessage = "Kamera tidak dapat dibuka."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            if (hasBothCameras) {
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                    },
                    enabled = !isCapturing
                ) {
                    Icon(Icons.Filled.Cameraswitch, contentDescription = "Ganti kamera", tint = Color.White)
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            val capture = imageCapture
            FilledIconButton(
                onClick = {
                    if (capture == null) return@FilledIconButton
                    isCapturing = true
                    takePhoto(
                        context = context,
                        imageCapture = capture,
                        mirror = lensFacing == CameraSelector.LENS_FACING_FRONT,
                        onSaved = { uri ->
                            isCapturing = false
                            onPhotoCaptured(uri)
                        },
                        onError = { message ->
                            isCapturing = false
                            errorMessage = message
                        }
                    )
                },
                enabled = capture != null && !isCapturing,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = "Ambil foto",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Foto disimpan di DIREKTORI CACHE aplikasi (scoped storage): tanpa izin,
// tidak terlihat aplikasi lain, dan tidak memenuhi galeri pengguna.
private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    mirror: Boolean,
    onSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    // Kamera depan: hasil dicerminkan agar sama dengan yang terlihat di preview
    val metadata = ImageCapture.Metadata().apply { isReversedHorizontal = mirror }
    val options = ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()

    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),     // callback di main thread: aman mengubah state
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                // Hasil jepretan sebelumnya tidak diperlukan lagi
                dir.listFiles()?.filter { it != file }?.forEach { it.delete() }
                onSaved(Uri.fromFile(file).toString())
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Gagal mengambil foto", exception)
                onError("Gagal mengambil foto. Coba lagi.")
            }
        }
    )
}

// ProcessCameraProvider.getInstance() mengembalikan ListenableFuture;
// await() mengubahnya menjadi suspend function seperti Task.await() pada Firebase.
private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    ProcessCameraProvider.getInstance(this).await()
