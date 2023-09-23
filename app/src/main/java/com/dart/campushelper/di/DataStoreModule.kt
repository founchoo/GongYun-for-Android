package com.dart.campushelper.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.dart.campushelper.api.DataStoreService
import com.dart.campushelper.data.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private val Context.dataStore by preferencesDataStore(DataStoreRepository.PREFS_NAME)

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStoreService =
        DataStoreRepository(context.dataStore)
}