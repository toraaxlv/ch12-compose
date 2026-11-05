package com.example.ch12.data.photo

import com.example.ch12.data.auth.AuthRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// Struktur berkas:  users/{uid}/profile.jpg  (sejajar dengan path Firestore)
@Singleton
@Suppress("unused")
class FirebasePhotoStorage @Inject constructor(
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository
) : PhotoStorage {

    // TODO [T4.3] Ubah UploadTask (callback) menjadi Flow, seperti snapshot listener di Ch11:
    //   callbackFlow {
    //     val uid = authRepository.currentUser?.uid ?: throw NotSignedInException()
    //     val ref = storage.reference.child("users/$uid/profile.jpg")     // harus sama dengan storage.rules
    //     val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
    //     val task = ref.putBytes(bytes, metadata)
    //       .addOnProgressListener { s -> trySend(UploadEvent.Progress(s.bytesTransferred, s.totalByteCount)) }
    //       .addOnSuccessListener  { launch { send(Done(ref.downloadUrl.await().toString())); close() } }
    //       .addOnFailureListener  { e -> close(PhotoUploadException(storageErrorMessage(e), e)) }
    //     awaitClose { if (!task.isComplete) task.cancel() }    // batal = upload dihentikan
    //   }
    //   Bungkus send(Done) dengan try/catch: downloadUrl juga bisa gagal.
    override fun uploadProfilePhoto(bytes: ByteArray): Flow<UploadEvent> =
        flow { throw PhotoUploadException("Belum diimplementasikan: TODO [T4.3]") }
}

@Suppress("unused")
private fun storageErrorMessage(e: Exception): String =
    if (e is StorageException) storageErrorMessageForCode(e.errorCode)
    else e.message ?: "Upload gagal. Coba lagi."
