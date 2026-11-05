package com.example.ch12.util

// Batas minimal dari Firebase Authentication
const val MIN_PASSWORD_LENGTH = 6

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

// Validasi di perangkat hanya untuk umpan balik cepat. Keputusan akhir
// tetap di server (Firebase Authentication).
// Mengembalikan pesan error, atau null jika valid.
fun validateLogin(email: String, password: String): String? = when {
    email.isBlank() || password.isBlank() -> "Email dan password wajib diisi."
    !isValidEmail(email) -> "Format email tidak valid."
    else -> null
}

fun validateRegistration(email: String, password: String, confirmPassword: String): String? = when {
    email.isBlank() || password.isBlank() -> "Email dan password wajib diisi."
    !isValidEmail(email) -> "Format email tidak valid."
    password.length < MIN_PASSWORD_LENGTH -> "Password minimal $MIN_PASSWORD_LENGTH karakter."
    password != confirmPassword -> "Konfirmasi password tidak sama."
    else -> null
}
