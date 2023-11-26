package com.dart.campushelper.api

import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie

interface UserPreferenceService {
    /**
     * Change section.
     */
    suspend fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean)

    suspend fun changeIsYearDisplay(isYearDisplay: Boolean)

    suspend fun changeIsDateDisplay(isDateDisplay: Boolean)

    suspend fun changeIsTimeDisplay(isPin: Boolean)

    suspend fun changeCookies(cookies: List<Cookie>)

    suspend fun changeIsLogin(isLogin: Boolean)

    suspend fun changeSelectedDarkMode(darkMode: String)

    suspend fun changeEnableSystemColor(enable: Boolean)

    suspend fun changeUsername(username: String)

    suspend fun changePassword(password: String)

    suspend fun changeSemesterYearAndNo(semesterYearAndNo: String)

    suspend fun changeEnterUniversityYear(enterUniversityYear: String)

    /**
     * Observe section.
     */
    fun observeIsOtherCourseDisplay(): Flow<Boolean>

    fun observeIsYearDisplay(): Flow<Boolean>

    fun observeIsDateDisplay(): Flow<Boolean>

    fun observeIsTimeDisplay(): Flow<Boolean>

    fun observeCookies(): Flow<List<Cookie>>

    fun observeIsLogin(): Flow<Boolean>

    fun observeSelectedDarkMode(): Flow<String>

    fun observeDayOfWeek(): Flow<Int>

    fun observeEnableSystemColor(): Flow<Boolean?>
    
    fun observeUsername(): Flow<String>

    fun observePassword(): Flow<String>

    fun observeSemesterYearAndNo(): Flow<String>

    fun observeEnterUniversityYear(): Flow<String>
}