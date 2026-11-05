package com.example.ch12.di

import com.example.ch12.data.auth.AuthRepository
import com.example.ch12.data.auth.FirebaseAuthRepository
import com.example.ch12.data.note.FirestoreNoteRepository
import com.example.ch12.data.note.NoteRepository
import com.example.ch12.data.photo.AndroidImageCompressor
import com.example.ch12.data.photo.FirebasePhotoStorage
import com.example.ch12.data.photo.ImageCompressor
import com.example.ch12.data.photo.PhotoStorage
import com.example.ch12.data.profile.FirestoreProfileRepository
import com.example.ch12.data.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Satu-satunya tempat yang tahu bahwa implementasinya Firebase / Android
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    abstract fun bindNoteRepository(impl: FirestoreNoteRepository): NoteRepository

    // Chapter 12
    @Binds
    abstract fun bindProfileRepository(impl: FirestoreProfileRepository): ProfileRepository

    @Binds
    abstract fun bindPhotoStorage(impl: FirebasePhotoStorage): PhotoStorage

    @Binds
    abstract fun bindImageCompressor(impl: AndroidImageCompressor): ImageCompressor
}
