package com.example.ch12.di

import com.example.ch12.util.Clock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideClock(): Clock = Clock { System.currentTimeMillis() }
}
