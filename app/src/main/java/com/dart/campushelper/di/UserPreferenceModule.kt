package com.dart.campushelper.di

import com.dart.campushelper.api.DataStoreService
import com.dart.campushelper.api.UserPreferenceService
import com.dart.campushelper.data.UserPreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserPreferenceModule {

    @Singleton
    @Provides
    fun provideUserPreferenceRepository(
        dataStoreService: DataStoreService,
    ): UserPreferenceService = UserPreferenceRepository(dataStoreService)
}