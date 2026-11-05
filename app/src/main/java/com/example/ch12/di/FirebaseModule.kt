package com.example.ch12.di

import com.example.ch12.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 10.0.2.2 = localhost komputer, dilihat dari emulator Android.
// Untuk HP fisik, ganti dengan IP komputer di jaringan yang sama.
private const val EMULATOR_HOST = "10.0.2.2"

// Class SDK Firebase tidak bisa diberi @Inject, jadi disediakan lewat @Provides.
// Firebase diinisialisasi otomatis dari google-services.json saat aplikasi dibuka.
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, 9099)
    }

    // Offline persistence aktif secara default di Android
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, 8080)
    }

    // Chapter 12: bucket dibaca dari storage_bucket di google-services.json
    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance().apply {
        // Default-nya 10 menit mencoba ulang saat offline; terlalu lama untuk
        // pengguna yang menatap progress bar. Setelah 30 detik -> error + "Coba lagi".
        maxUploadRetryTimeMillis = 30_000
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, 9199)
    }
}
