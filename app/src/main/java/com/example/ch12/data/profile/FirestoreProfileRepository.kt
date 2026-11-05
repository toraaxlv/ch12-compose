package com.example.ch12.data.profile

import android.util.Log
import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.model.UserProfile
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreProfileRepo"

// Struktur data:  users/{uid}  { photoUrl, photoUpdatedAt }
@Singleton
class FirestoreProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : ProfileRepository {

    private fun profileOf(uid: String): DocumentReference =
        firestore.collection("users").document(uid)

    // Sama dengan observeNotes(): listener berganti mengikuti akun
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeProfile(): Flow<UserProfile> = authRepository.authState
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid -> if (uid == null) flowOf(UserProfile()) else profileSnapshots(uid) }

    private fun profileSnapshots(uid: String): Flow<UserProfile> = callbackFlow {
        val registration = profileOf(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Foto profil bukan data penting: cukup dicatat, daftar catatan tetap jalan
                Log.w(TAG, "Profil gagal dibaca: ${error.message}")
                trySend(UserProfile())
                return@addSnapshotListener
            }
            trySend(profileFromMap(snapshot?.data))
        }
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    // TODO [T4.5] Simpan REFERENSI foto ke users/{uid}, TANPA await() (sama dengan Ch11):
    //   runCatching {
    //     val uid = authRepository.currentUser?.uid ?: throw NotSignedInException()
    //     profileOf(uid).set(photoFieldsOf(photoUrl, updatedAt), SetOptions.merge())
    //       .addOnFailureListener { e -> Log.w(TAG, "Profil ditolak server: ${e.message}") }
    //   }
    //   merge(): field lain di dokumen (jika nanti ada) tidak ikut terhapus.
    override fun updatePhotoUrl(photoUrl: String, updatedAt: Long): Result<Unit> =
        Result.failure(NotImplementedError("Belum diimplementasikan: TODO [T4.5]"))
}
