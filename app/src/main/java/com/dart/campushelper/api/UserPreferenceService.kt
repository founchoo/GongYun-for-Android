package com.dart.campushelper.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface UserPreferenceService {
    /**
     * Change section.
     */
    suspend fun changeIsLogin(isLogin: Boolean)

    suspend fun changeSelectedDarkMode(darkMode: String)

    suspend fun changeStartLocalDate(localDate: LocalDate)

    suspend fun changeEnableSystemColor(enable: Boolean)

    suspend fun changeIsPin(isPin: Boolean)

    suspend fun changeUsername(username: String)

    suspend fun changePassword(password: String)

    suspend fun changeSemesterYearAndNo(semesterYearAndNo: String)

    suspend fun changeEnterUniversityYear(enterUniversityYear: String)

    /**
     * Observe section.
     */
    fun observeIsLogin(): StateFlow<Boolean>

    fun observeSelectedDarkMode(): Flow<String>

    fun observeStartLocalDate(): Flow<LocalDate?>

    fun observeDayOfWeek(): Flow<Int>

    fun observeEnableSystemColor(): Flow<Boolean?>

    fun observeIsPin(): Flow<Boolean?>

    fun observeUsername(): Flow<String>

    fun observePassword(): Flow<String>

    fun observeSemesterYearAndNo(): Flow<String>

    fun observeEnterUniversityYear(): Flow<String>
}