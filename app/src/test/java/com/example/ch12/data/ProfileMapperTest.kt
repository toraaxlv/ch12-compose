package com.example.ch12.data

import com.example.ch12.data.model.UserProfile
import com.example.ch12.data.profile.FIELD_PHOTO_UPDATED_AT
import com.example.ch12.data.profile.FIELD_PHOTO_URL
import com.example.ch12.data.profile.photoFieldsOf
import com.example.ch12.data.profile.profileFromMap
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileMapperTest {

    // Tepat dua field, sesuai hasOnly() di firestore.rules
    @Test fun `field yang ditulis sesuai rules`() {
        val map = photoFieldsOf("https://x/profile.jpg", 1_000L)
        assertEquals(setOf(FIELD_PHOTO_URL, FIELD_PHOTO_UPDATED_AT), map.keys)
        assertEquals("https://x/profile.jpg", map[FIELD_PHOTO_URL])
        assertEquals(1_000L, map[FIELD_PHOTO_UPDATED_AT])
    }

    @Test fun `dokumen belum ada menjadi profil kosong`() {
        assertEquals(UserProfile(), profileFromMap(null))
    }

    @Test fun `angka dari Firestore dibaca sebagai Long`() {
        val profile = profileFromMap(mapOf(FIELD_PHOTO_URL to "https://x", FIELD_PHOTO_UPDATED_AT to 7))
        assertEquals(UserProfile("https://x", 7L), profile)
    }

    @Test fun `tipe salah atau URL kosong tidak membuat crash`() {
        assertEquals(UserProfile(), profileFromMap(mapOf(FIELD_PHOTO_URL to 42, FIELD_PHOTO_UPDATED_AT to "kemarin")))
        assertEquals(UserProfile(), profileFromMap(mapOf(FIELD_PHOTO_URL to "  ")))
    }
}
