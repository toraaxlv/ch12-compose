package com.example.ch12.data.auth

import com.example.ch12.data.model.AuthUser
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    // callbackFlow mengubah listener berbasis callback menjadi Flow.
    // Listener langsung dipanggil sekali saat didaftarkan (status saat ini).
    override val authState: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        // Saat Flow dibatalkan, listener dilepas: mencegah memory leak
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override val currentUser: AuthUser?
        get() = auth.currentUser?.toAuthUser()

    override suspend fun login(email: String, password: String): Result<AuthUser> =
        authenticate { auth.signInWithEmailAndPassword(email, password).await() }

    // createUser... otomatis membuat pengguna langsung login
    override suspend fun register(email: String, password: String): Result<AuthUser> =
        authenticate { auth.createUserWithEmailAndPassword(email, password).await() }

    // ID token dari Google ditukar menjadi identitas Firebase
    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> =
        authenticate {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
        }

    override fun logout() = auth.signOut()

    // Login MENUNGGU server (await): tanpa server, identitas tidak bisa diverifikasi.
    // Exception Firebase diterjemahkan menjadi pesan yang bisa dibaca pengguna.
    private suspend fun authenticate(block: suspend () -> AuthResult): Result<AuthUser> =
        try {
            val user = block().user ?: error("Pengguna tidak ditemukan")
            Result.success(user.toAuthUser())
        } catch (e: CancellationException) {
            throw e                        // pembatalan coroutine jangan ditelan
        } catch (e: Exception) {
            Result.failure(AuthException(authErrorMessage(e), e))
        }
}

class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun FirebaseUser.toAuthUser() = AuthUser(uid = uid, email = email, displayName = displayName)
