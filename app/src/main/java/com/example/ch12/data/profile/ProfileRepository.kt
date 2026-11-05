package com.example.ch12.data.profile

import com.example.ch12.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    // Profil pengguna yang sedang login; UserProfile() kosong jika belum ada / logout
    fun observeProfile(): Flow<UserProfile>

    // Seperti penulisan catatan: tidak menunggu server (Chapter 11)
    fun updatePhotoUrl(photoUrl: String, updatedAt: Long): Result<Unit>
}
