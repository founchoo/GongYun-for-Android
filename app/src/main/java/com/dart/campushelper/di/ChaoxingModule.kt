package com.dart.campushelper.di

import com.dart.campushelper.api.ChaoxingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ChaoxingModule {

    @Singleton
    @Provides
    fun provideChaoxingService(): ChaoxingService {
        return ChaoxingService.create()
    }
}