package com.example.ch12.data.auth

import com.example.ch12.data.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // null = belum login. Memancarkan nilai baru setiap status login berubah.
    val authState: Flow<AuthUser?>

    // Nilai saat ini, tersedia segera saat aplikasi dibuka (dibaca dari disk)
    val currentUser: AuthUser?

    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String): Result<AuthUser>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    fun logout()
}
