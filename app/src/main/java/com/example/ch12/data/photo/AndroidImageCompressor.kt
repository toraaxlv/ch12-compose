package com.example.ch12.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.ch12.util.PROFILE_PHOTO_QUALITY
import com.example.ch12.util.PROFILE_PHOTO_SIZE
import com.example.ch12.util.calculateInSampleSize
import com.example.ch12.util.centerSquare
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject

// Foto 4000x3000 -> JPEG persegi 400x400, sekitar 30-60 KB
class AndroidImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageCompressor {

    // Decode dan kompres JPEG adalah kerja CPU + I/O: jangan di main thread
    override suspend fun compress(uri: String): CompressedImage = withContext(Dispatchers.Default) {
        val source = Uri.parse(uri)

        // Tahap 1: baca dimensi saja. Tidak ada piksel yang dialokasikan,
        // jadi foto 12 MP tidak langsung memakan 48 MB.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        open(source).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw ImageDecodeException("Berkas yang dipilih bukan gambar.")
        }

        // Tahap 2: decode dengan inSampleSize. Stream HARUS dibuka ulang.
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, PROFILE_PHOTO_SIZE)
        }
        val sampled = open(source).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw ImageDecodeException("Berkas yang dipilih bukan gambar.")

        // Potong persegi di tengah, perkecil ke 400 px, lalu putar sesuai EXIF
        // (BitmapFactory mengabaikan tag orientasi, jadi foto potret bisa miring).
        val crop = centerSquare(sampled.width, sampled.height)
        val scale = minOf(1f, PROFILE_PHOTO_SIZE.toFloat() / crop.size)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(readRotation(source).toFloat())
        }
        val result = Bitmap.createBitmap(sampled, crop.left, crop.top, crop.size, crop.size, matrix, true)

        val out = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, PROFILE_PHOTO_QUALITY, out)
        val bytes = out.toByteArray()

        val compressed = CompressedImage(
            bytes = bytes,
            originalWidth = bounds.outWidth,
            originalHeight = bounds.outHeight,
            width = result.width,
            height = result.height
        )

        // Bitmap besar ditahan sampai GC kalau tidak dilepas manual.
        if (result !== sampled) result.recycle()
        sampled.recycle()
        compressed
    }

    // content:// (Photo Picker) maupun file:// (CameraX) dibuka lewat ContentResolver
    private fun open(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw ImageDecodeException("Gambar tidak dapat dibuka.")

    // Kamera menyimpan piksel apa adanya + tag orientasi. BitmapFactory
    // mengabaikan tag itu, sehingga tanpa ini foto potret bisa tampil miring.
    private fun readRotation(uri: Uri): Int = runCatching {
        open(uri).use { ExifInterface(it).rotationDegrees }
    }.getOrDefault(0)
}
