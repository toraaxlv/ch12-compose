package com.example.ch12.data.photo

// Hasil kompresi beserta ukuran sebelum/sesudah, untuk ditampilkan di layar
class CompressedImage(
    val bytes: ByteArray,
    val originalWidth: Int,
    val originalHeight: Int,
    val width: Int,
    val height: Int
)

// URI ditulis sebagai String agar ViewModel bisa diuji di JVM
// (android.net.Uri tidak tersedia di unit test).
interface ImageCompressor {
    suspend fun compress(uri: String): CompressedImage
}

class ImageDecodeException(message: String) : Exception(message)
