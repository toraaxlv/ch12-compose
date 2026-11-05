package com.example.ch12.data.photo

import com.google.firebase.storage.StorageException

// Fungsi murni: mudah diuji di JVM. Daftar kode: StorageException.ERROR_*
fun storageErrorMessageForCode(code: Int): String = when (code) {
    StorageException.ERROR_NOT_AUTHENTICATED -> "Sesi berakhir. Silakan masuk lagi."
    StorageException.ERROR_NOT_AUTHORIZED ->
        "Upload ditolak server. Periksa storage.rules (pemilik, ukuran < 1 MB, image/jpeg)."
    StorageException.ERROR_BUCKET_NOT_FOUND,
    StorageException.ERROR_PROJECT_NOT_FOUND ->
        "Cloud Storage belum diaktifkan, atau google-services.json bukan dari proyek ini."
    StorageException.ERROR_QUOTA_EXCEEDED -> "Kuota penyimpanan habis."
    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "Koneksi terputus. Periksa internet lalu coba lagi."
    StorageException.ERROR_CANCELED -> "Upload dibatalkan."
    else -> "Upload gagal. Coba lagi."
}
