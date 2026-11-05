package com.example.ch12.testutil

import com.example.ch12.data.model.UserProfile
import com.example.ch12.data.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Pengganti dokumen users/{uid} di Firestore
class FakeProfileRepository(initial: UserProfile = UserProfile()) : ProfileRepository {

    val profile = MutableStateFlow(initial)
    var writeError: Exception? = null

    override fun observeProfile(): Flow<UserProfile> = profile

    override fun updatePhotoUrl(photoUrl: String, updatedAt: Long): Result<Unit> {
        writeError?.let { return Result.failure(it) }
        profile.value = UserProfile(photoUrl, updatedAt)
        return Result.success(Unit)
    }
}
