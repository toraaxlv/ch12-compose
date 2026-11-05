package com.example.ch12.data.photo

import kotlinx.coroutines.flow.Flow

sealed interface UploadEvent {
    data class Progress(val bytesTransferred: Long, val totalBytes: Long) : UploadEvent {
        val fraction: Float
            get() = if (totalBytes > 0) bytesTransferred.toFloat() / totalBytes else 0f
    }

    data class Done(val downloadUrl: String) : UploadEvent
}

interface PhotoStorage {
    // Flow: upload adalah operasi panjang dengan banyak nilai antara (progres),
    // lalu satu nilai akhir (URL). Gagal -> Flow melempar exception.
    // Membatalkan pengumpulan Flow = membatalkan upload.
    fun uploadProfilePhoto(bytes: ByteArray): Flow<UploadEvent>
}

class PhotoUploadException(message: String, cause: Throwable? = null) : Exception(message, cause)
