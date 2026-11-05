package com.example.ch12.testutil

import com.example.ch12.data.auth.AuthException
import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Pengganti Firebase Authentication. Akun disimpan di Map: email -> password.
class FakeAuthRepository(
    initialUser: AuthUser? = null,
    accounts: Map<String, String> = emptyMap()
) : AuthRepository {

    private val user = MutableStateFlow(initialUser)
    private val accounts = accounts.toMutableMap()

    // Jika diisi, semua operasi gagal dengan pesan ini (mis. offline)
    var failureMessage: String? = null
    var loginCalls = 0
        private set
    var lastGoogleToken: String? = null
        private set

    override val authState: Flow<AuthUser?> = user
    override val currentUser: AuthUser? get() = user.value

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        loginCalls++
        failureMessage?.let { return Result.failure(AuthException(it)) }
        if (accounts[email] != password) return Result.failure(AuthException("Email atau password salah."))
        return Result.success(signIn(email))
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> {
        failureMessage?.let { return Result.failure(AuthException(it)) }
        if (email in accounts) return Result.failure(AuthException("Email sudah terdaftar. Silakan masuk."))
        accounts[email] = password
        return Result.success(signIn(email))
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        lastGoogleToken = idToken
        failureMessage?.let { return Result.failure(AuthException(it)) }
        return Result.success(signIn("google@example.com"))
    }

    override fun logout() {
        user.value = null
    }

    private fun signIn(email: String) = AuthUser(uid = "uid-$email", email = email).also { user.value = it }
}
