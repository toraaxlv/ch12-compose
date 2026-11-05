package com.example.ch12.data.profile

import com.example.ch12.data.model.UserProfile

// Sama persis dengan yang diperiksa isValidProfile() di firestore.rules
const val FIELD_PHOTO_URL = "photoUrl"
const val FIELD_PHOTO_UPDATED_AT = "photoUpdatedAt"

fun photoFieldsOf(photoUrl: String, updatedAt: Long): Map<String, Any> = mapOf(
    FIELD_PHOTO_URL to photoUrl,
    FIELD_PHOTO_UPDATED_AT to updatedAt
)

// Dokumen belum ada (pengguna baru) -> data null -> profil kosong
fun profileFromMap(data: Map<String, Any?>?): UserProfile = UserProfile(
    photoUrl = (data?.get(FIELD_PHOTO_URL) as? String)?.takeIf { it.isNotBlank() },
    photoUpdatedAt = (data?.get(FIELD_PHOTO_UPDATED_AT) as? Number)?.toLong() ?: 0L
)
