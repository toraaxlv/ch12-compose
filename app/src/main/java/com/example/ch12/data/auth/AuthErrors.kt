package com.example.ch12.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException

fun authErrorMessage(e: Throwable): String = when {
    // Kesalahan setup yang paling sering: google-services.json masih placeholder
    // atau berasal dari proyek lain
    e.message.orEmpty().contains("API key not valid") ->
        "Konfigurasi Firebase tidak valid. Ganti app/google-services.json dengan file dari Firebase Console."
    e is FirebaseNetworkException -> "Tidak ada koneksi internet."
    e is FirebaseTooManyRequestsException -> authErrorMessageForCode("ERROR_TOO_MANY_REQUESTS")
    e is FirebaseAuthException -> authErrorMessageForCode(e.errorCode)
    else -> e.message ?: "Terjadi kesalahan. Coba lagi."
}

// Fungsi murni (tanpa SDK): mudah diuji di JVM.
// Daftar kode: FirebaseAuthException.getErrorCode()
fun authErrorMessageForCode(code: String?): String = when (code) {
    "ERROR_INVALID_EMAIL" -> "Format email tidak valid."
    // Sejak email enumeration protection aktif (default proyek baru), Firebase
    // tidak lagi membedakan "email tidak terdaftar" dan "password salah".
    "ERROR_INVALID_CREDENTIAL",
    "ERROR_WRONG_PASSWORD",
    "ERROR_USER_NOT_FOUND" -> "Email atau password salah."
    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar. Silakan masuk."
    "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah (minimal 6 karakter)."
    "ERROR_USER_DISABLED" -> "Akun ini dinonaktifkan."
    "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Tunggu sebentar lalu coba lagi."
    "ERROR_OPERATION_NOT_ALLOWED" ->
        "Metode login ini belum diaktifkan di Firebase Console (Authentication > Sign-in method)."
    else -> "Autentikasi gagal. Coba lagi."
}
