package com.example.ch12.data.photo

import android.content.Context
import android.net.Uri
// Butuh untuk T4.2: android.graphics.{Bitmap, BitmapFactory, Matrix}, java.io.ByteArrayOutputStream,
// com.example.ch12.util.{PROFILE_PHOTO_SIZE, PROFILE_PHOTO_QUALITY, calculateInSampleSize, centerSquare}
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

// Foto 4000x3000 -> JPEG persegi 400x400, sekitar 30-60 KB
class AndroidImageCompressor @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageCompressor {

    // Decode dan kompres JPEG adalah kerja CPU + I/O: jangan di main thread
    override suspend fun compress(uri: String): CompressedImage = withContext(Dispatchers.Default) {
        @Suppress("UNUSED_VARIABLE")
        val source = Uri.parse(uri)

        // TODO [T4.2] Dekode dua tahap (lihat Bab 12, "Berapa Besar Sebuah Foto di Memori?"):
        //   1. val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        //      open(source).use { BitmapFactory.decodeStream(it, null, bounds) }
        //      outWidth/outHeight <= 0 -> throw ImageDecodeException("Berkas yang dipilih bukan gambar.")
        //   2. Options().apply { inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight,
        //      PROFILE_PHOTO_SIZE) } -> decodeStream lagi (stream BARU) -> sampled
        //   3. Matrix: postScale(minOf(1f, PROFILE_PHOTO_SIZE.toFloat() / sisi)) lalu
        //      postRotate(readRotation(source).toFloat())
        //      Bitmap.createBitmap(sampled, x, y, w, h, matrix, true)
        //      TODO [T6.2] (Tantangan) x, y, w, h dari centerSquare(...) agar hasilnya persegi.
        //      Sebelum T6.2: pakai 0, 0, sampled.width, sampled.height dan sisi = maxOf(width, height).
        //   4. ByteArrayOutputStream -> bitmap.compress(JPEG, PROFILE_PHOTO_QUALITY, out)
        //   5. recycle() bitmap, kembalikan CompressedImage(bytes, bounds.outWidth, bounds.outHeight,
        //      hasil.width, hasil.height)
        TODO("T4.2 compress")
    }

    // content:// (Photo Picker) maupun file:// (CameraX) dibuka lewat ContentResolver
    @Suppress("unused")
    private fun open(uri: Uri): InputStream =
        context.contentResolver.openInputStream(uri)
            ?: throw ImageDecodeException("Gambar tidak dapat dibuka.")

    // Kamera menyimpan piksel apa adanya + tag orientasi. BitmapFactory
    // mengabaikan tag itu, sehingga tanpa ini foto potret bisa tampil miring.
    @Suppress("unused")
    private fun readRotation(uri: Uri): Int = runCatching {
        open(uri).use { ExifInterface(it).rotationDegrees }
    }.getOrDefault(0)
}
