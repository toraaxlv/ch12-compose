package com.example.ch12.data.model

// Dokumen users/{uid}. Hanya REFERENSI ke foto; berkasnya di Cloud Storage.
// photoUpdatedAt dipakai sebagai kunci cache Coil: URL bisa sama walau isinya baru.
data class UserProfile(
    val photoUrl: String? = null,
    val photoUpdatedAt: Long = 0L
)
