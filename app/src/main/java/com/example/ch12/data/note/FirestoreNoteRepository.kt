package com.example.ch12.data.note

import android.util.Log
import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.model.Note
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreNoteRepo"

// Struktur data:  users/{uid}/notes/{noteId}
@Singleton
class FirestoreNoteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : NoteRepository {

    private fun notesOf(uid: String): CollectionReference =
        firestore.collection("users").document(uid).collection("notes")

    private fun currentNotes(): CollectionReference =
        notesOf(authRepository.currentUser?.uid ?: throw NotSignedInException())

    // Listener mengikuti status login: ganti akun = ganti collection,
    // logout = daftar kosong (listener lama dilepas oleh flatMapLatest).
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeNotes(): Flow<List<Note>> = authRepository.authState
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else notesSnapshots(uid) }

    // Pull vs push: tidak ada polling. Server MENDORONG setiap perubahan.
    private fun notesSnapshots(uid: String): Flow<List<Note>> = callbackFlow {
        val registration = notesOf(uid)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            // INCLUDE: listener juga dipanggil saat server MENGONFIRMASI penulisan,
            // sehingga tanda "menunggu sinkron" di kartu bisa hilang.
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(NoteAccessException(firestoreErrorMessage(error), error))
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents.orEmpty().map { doc ->
                    noteFromMap(doc.id, doc.data.orEmpty(), doc.metadata.hasPendingWrites())
                }
                trySend(sortNotes(notes))
            }
        awaitClose { registration.remove() }
    }

    // get() saat offline otomatis membaca cache lokal
    override suspend fun getNote(id: String): Note? {
        val doc = currentNotes().document(id).get().await()
        return if (doc.exists()) noteFromMap(doc.id, doc.data.orEmpty()) else null
    }

    // Penulisan TIDAK di-await. Task Firestore baru selesai setelah SERVER
    // mengonfirmasi; saat offline itu bisa berjam-jam. Perubahan sudah langsung
    // masuk cache lokal dan listener langsung memancarkan daftar baru
    // (latency compensation), jadi UI tidak perlu menunggu.
    override fun addNote(note: Note): Result<String> = runCatching {
        val doc = currentNotes().document()          // ID dibuat di perangkat, tanpa jaringan
        doc.set(note.toFirestoreMap()).logFailure("add")
        doc.id
    }

    override fun updateNote(note: Note): Result<Unit> = runCatching {
        currentNotes().document(note.id).set(note.toFirestoreMap()).logFailure("update")
    }

    override fun deleteNote(id: String): Result<Unit> = runCatching {
        currentNotes().document(id).delete().logFailure("delete")
    }

    // Penolakan server (mis. Security Rules) baru diketahui belakangan.
    // Saat itu cache lokal dikembalikan dan listener memancarkan data yang benar.
    private fun Task<Void>.logFailure(action: String) {
        addOnFailureListener { e -> Log.w(TAG, "$action ditolak server: ${e.message}") }
    }
}

class NoteAccessException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun firestoreErrorMessage(e: FirebaseFirestoreException): String = when (e.code) {
    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
        "Akses ditolak server. Periksa Security Rules di Firebase Console."
    FirebaseFirestoreException.Code.UNAVAILABLE -> "Server tidak dapat dihubungi."
    else -> e.message ?: "Gagal memuat catatan."
}
