/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dart.campushelper.data.app

import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie
import java.time.LocalDate

/**
 * Interface to the Interests data layer.
 */
interface AppRepository {
    /**
     * Change section.
     */
    suspend fun changeIsLogin(isLogin: Boolean)

    suspend fun changeSelectedDarkMode(darkMode: String)

    suspend fun changeStartLocalDate(localDate: LocalDate)

    suspend fun changeCurrentWeek(week: Int)

    suspend fun changeEnableSystemColor(enable: Boolean)

    suspend fun changeCookies(cookies: Set<Cookie>)

    suspend fun changeIsPin(isPin: Boolean)

    suspend fun changeUsername(username: String)

    suspend fun changeSemesterYearAndNo(semesterYearAndNo: String)

    suspend fun changeEnterUniversityYear(enterUniversityYear: String)

    /**
     * Observe section.
     */
    fun observeIsLogin(): Flow<Boolean?>

    fun observeSelectedDarkMode(): Flow<String>

    fun observeStartLocalDate(): Flow<LocalDate?>

    fun observeCurrentWeek(): Flow<Int>

    fun observeDayOfWeek(): Flow<Int>

    fun observeEnableSystemColor(): Flow<Boolean?>

    fun observeCookies(): Flow<Set<Cookie>>

    fun observeIsPin(): Flow<Boolean?>

    fun observeUsername(): Flow<String>

    fun observeSemesterYearAndNo(): Flow<String>

    fun observeEnterUniversityYear(): Flow<String>
}
