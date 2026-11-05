package com.example.ch12.util

import java.util.Locale

// Fungsi murni tanpa Bitmap: mudah diuji di JVM.

// Foto profil cukup 400x400 piksel: puluhan KB, bukan beberapa MB.
const val PROFILE_PHOTO_SIZE = 400
const val PROFILE_PHOTO_QUALITY = 80

// Saat didekode, setiap piksel butuh 4 byte (ARGB_8888).
// 4000 x 3000 x 4 = 48 MB, walau berkas JPEG-nya hanya 3 MB.
fun decodedSizeBytes(width: Int, height: Int): Long = width.toLong() * height * 4

// inSampleSize = n berarti hanya 1 dari n piksel (per sisi) yang didekode.
// Dipilih pangkat 2 terbesar yang membuat sisi TERPENDEK masih >= reqSize,
// karena foto profil dipotong persegi dari sisi terpendek.
// TODO [T4.1] Mulai dari 1; selama minOf(width, height) / (inSampleSize * 2) >= reqSize,
//   kalikan inSampleSize dengan 2. Contoh: (4000, 3000, 400) -> 4.
fun calculateInSampleSize(width: Int, height: Int, reqSize: Int): Int =
    TODO("T4.1 calculateInSampleSize")

data class SquareCrop(val left: Int, val top: Int, val size: Int)

// Persegi terbesar di tengah gambar
// TODO [T6.1] (Tantangan) size = sisi terpendek; left/top = sisa lebar/tinggi dibagi 2.
//   Contoh: (4000, 3000) -> SquareCrop(left = 500, top = 0, size = 3000).
fun centerSquare(width: Int, height: Int): SquareCrop =
    TODO("T6.1 centerSquare")

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale("id", "ID"), "%.1f MB", bytes / (1024.0 * 1024.0))
}
