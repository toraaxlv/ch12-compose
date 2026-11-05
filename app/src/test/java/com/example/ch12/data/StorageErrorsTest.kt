package com.example.ch12.data

import com.example.ch12.data.photo.storageErrorMessageForCode
import com.google.firebase.storage.StorageException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageErrorsTest {

    @Test fun `ditolak rules menyebut storage rules`() {
        assertTrue(storageErrorMessageForCode(StorageException.ERROR_NOT_AUTHORIZED).contains("storage.rules"))
    }

    @Test fun `bucket tidak ada menyebut Cloud Storage`() {
        assertTrue(storageErrorMessageForCode(StorageException.ERROR_BUCKET_NOT_FOUND).contains("Cloud Storage"))
    }

    @Test fun `kode tidak dikenal memakai pesan umum`() {
        assertEquals("Upload gagal. Coba lagi.", storageErrorMessageForCode(123))
    }
}
