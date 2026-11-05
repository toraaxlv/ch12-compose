package com.example.ch12.testutil

import com.example.ch12.data.photo.PhotoStorage
import com.example.ch12.data.photo.PhotoUploadException
import com.example.ch12.data.photo.UploadEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Pengganti Cloud Storage. Upload "berhenti" di 50% sampai gate diselesaikan,
// sehingga tes bisa memeriksa state di tengah upload.
class FakePhotoStorage(
    private val downloadUrl: String = "https://storage.example/users/uid/profile.jpg"
) : PhotoStorage {

    var failureMessage: String? = null
    var gate: CompletableDeferred<Unit>? = null
    var uploadCalls = 0
        private set
    var lastBytes: ByteArray? = null
        private set

    override fun uploadProfilePhoto(bytes: ByteArray): Flow<UploadEvent> = flow {
        uploadCalls++
        lastBytes = bytes
        emit(UploadEvent.Progress(0, bytes.size.toLong()))
        emit(UploadEvent.Progress(bytes.size / 2L, bytes.size.toLong()))
        gate?.await()
        failureMessage?.let { throw PhotoUploadException(it) }
        emit(UploadEvent.Progress(bytes.size.toLong(), bytes.size.toLong()))
        emit(UploadEvent.Done(downloadUrl))
    }
}
