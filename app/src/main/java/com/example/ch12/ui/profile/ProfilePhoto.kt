package com.example.ch12.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

// Satu composable untuk tiga sumber: URL Storage (https://), hasil Photo Picker
// (content://), dan hasil kamera (file://). Coil mendekode sesuai ukuran tampilan
// (downsampling, Chapter 9), sehingga foto 12 MP tidak menghabiskan memori.
@Composable
fun ProfilePhoto(
    model: String?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    // URL Storage tetap sama walau fotonya diganti; versi membedakan cache-nya
    version: Long = 0L
) {
    val context = LocalContext.current
    val request = remember(model, version) {
        model?.let {
            ImageRequest.Builder(context)
                .data(it)
                .memoryCacheKey("$it#$version")
                .diskCacheKey("$it#$version")
                .crossfade(true)
                .build()
        }
    }
    val placeholder = rememberVectorPainter(Icons.Filled.Person)

    AsyncImage(
        model = request,
        contentDescription = "Foto profil",
        contentScale = ContentScale.Crop,
        placeholder = placeholder,
        fallback = placeholder,                                 // model == null
        error = rememberVectorPainter(Icons.Filled.BrokenImage),
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
    )
}
