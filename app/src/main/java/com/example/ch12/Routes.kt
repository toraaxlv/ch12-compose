package com.example.ch12

const val ARG_NOTE_ID = "noteId"
const val NEW_NOTE_ID = "new"

// Chapter 12: hasil layar kamera dikirim ke layar profil lewat SavedStateHandle
// entri sebelumnya (pola "hasil navigasi"), lalu dibaca ProfileViewModel.
const val KEY_CAPTURED_PHOTO = "capturedPhoto"

sealed class Routes(val route: String) {
    // Layar sebelum login
    data object Login : Routes("login")
    data object Register : Routes("register")

    // Layar setelah login
    data object NoteList : Routes("notes")

    // ID Firestore berupa String; "new" -> catatan baru
    data object NoteEdit : Routes("edit/{$ARG_NOTE_ID}") {
        fun createRoute(noteId: String = NEW_NOTE_ID) = "edit/$noteId"
    }

    // Chapter 12
    data object Profile : Routes("profile")
    data object Camera : Routes("camera")
}

// Layar yang hanya boleh dibuka saat BELUM login
val authRoutes = setOf(Routes.Login.route, Routes.Register.route)
