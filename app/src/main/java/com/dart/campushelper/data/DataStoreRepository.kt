package com.dart.campushelper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.api.DataStoreService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import java.time.LocalDate
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(DataStoreRepository.PREFS_NAME)

/**
 * Implementation of InterestRepository that returns a hardcoded list of
 * topics, people and publications synchronously.
 */
class DataStoreRepository @Inject constructor() : DataStoreService {

    // Used to make suspend functions that read and update state safe to call from any thread
    private val mutex = Mutex()

    override suspend fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_OTHER_COURSE_DISPLAY] = isOtherCourseDisplay
            }
        }
    }

    override suspend fun changeIsYearDisplay(isYearDisplay: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_YEAR_DISPLAY] = isYearDisplay
            }
        }
    }

    override suspend fun changeIsDateDisplay(isDateDisplay: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_DATE_DISPLAY] = isDateDisplay
            }
        }
    }

    override suspend fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_TIME_DISPLAY] = isTimeDisplay
            }
        }
    }

    override suspend fun changeCookies(cookies: List<Cookie>) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_COOKIES] = Gson().toJson(cookies)
            }
        }
    }

    override suspend fun changeIsLogin(isLogin: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_LOGIN] = isLogin
            }
        }
    }

    override suspend fun changeSelectedDarkMode(darkMode: String) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_SELECTED_DARK_MODE] = darkMode
            }
        }
    }

    override suspend fun changeEnableSystemColor(enable: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_ENABLE_SYSTEM_COLOR] = enable
            }
        }
    }

    override suspend fun changeUsername(username: String) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_USERNAME] = username
            }
        }
    }

    override suspend fun changePassword(password: String) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_PASSWORD] = password
            }
        }
    }

    override suspend fun changeSemesterYearAndNo(semesterYearAndNo: String) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_SEMESTER_YEAR_AND_NO] = semesterYearAndNo
            }
        }
    }

    override suspend fun changeEnterUniversityYear(enterUniversityYear: String) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_ENTER_UNIVERSITY_YEAR] = enterUniversityYear
            }
        }
    }

    override suspend fun changeIsScreenshotMode(isScreenshotMode: Boolean) {
        mutex.withLock {
            context.dataStore.edit {
                it[KEY_IS_SCREENSHOT_MODE] = isScreenshotMode
            }
        }
    }

    override fun observeIsOtherCourseDisplay(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_OTHER_COURSE_DISPLAY] ?: DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
    }

    override fun observeIsYearDisplay(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_YEAR_DISPLAY] ?: DEFAULT_VALUE_IS_YEAR_DISPLAY
    }

    override fun observeIsDateDisplay(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_DATE_DISPLAY] ?: DEFAULT_VALUE_IS_DATE_DISPLAY
    }

    override fun observeIsTimeDisplay(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_TIME_DISPLAY] ?: DEFAULT_VALUE_IS_TIME_DISPLAY
    }

    override fun observeCookies(): Flow<List<Cookie>> = context.dataStore.data.map {
        val json = it[KEY_COOKIES] ?: DEFAULT_VALUE_COOKIES
        if (json == DEFAULT_VALUE_COOKIES) {
            emptyList<Cookie>()
        } else {
            val typeOfT = object : TypeToken<List<Cookie>>() {}.type
            Gson().fromJson(json, typeOfT)
        }
    }

    override fun observeIsLogin(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_LOGIN] ?: DEFAULT_VALUE_IS_LOGIN
    }

    override fun observeSelectedDarkMode(): Flow<String> =
        context.dataStore.data.map {
            it[KEY_SELECTED_DARK_MODE] ?: DEFAULT_VALUE_SELECTED_DARK_MODE
        }

    override fun observeDayOfWeek(): Flow<Int> =
        context.dataStore.data.map { it[KEY_DAY_OF_WEEK] ?: DEFAULT_VALUE_DAY_OF_WEEK }

    override fun observeEnableSystemColor(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ENABLE_SYSTEM_COLOR] ?: DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
    }

    override fun observeUsername(): Flow<String> = context.dataStore.data.map {
        it[KEY_USERNAME] ?: DEFAULT_VALUE_USERNAME
    }

    override fun observePassword(): Flow<String> = context.dataStore.data.map {
        it[KEY_PASSWORD] ?: DEFAULT_VALUE_PASSWORD
    }

    override fun observeSemesterYearAndNo(): Flow<String> = context.dataStore.data.map {
        it[KEY_SEMESTER_YEAR_AND_NO] ?: DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
    }

    override fun observeEnterUniversityYear(): Flow<String> = context.dataStore.data.map {
        it[KEY_ENTER_UNIVERSITY_YEAR] ?: DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
    }

    override fun observeIsScreenshotMode(): Flow<Boolean> = context.dataStore.data.map {
        it[KEY_IS_SCREENSHOT_MODE] ?: DEFAULT_VALUE_IS_SCREENSHOT_MODE
    }

    companion object {
        const val PREFS_NAME = "user_datastore"

        val KEY_IS_OTHER_COURSE_DISPLAY = booleanPreferencesKey("is_other_course_display")
        val KEY_IS_YEAR_DISPLAY = booleanPreferencesKey("is_year_display")
        val KEY_IS_DATE_DISPLAY = booleanPreferencesKey("is_date_display")
        val KEY_IS_TIME_DISPLAY = booleanPreferencesKey("is_time_display")
        val KEY_COOKIES = stringPreferencesKey("cookies")
        val KEY_IS_LOGIN = booleanPreferencesKey("is_login")
        val KEY_DAY_OF_WEEK = intPreferencesKey("day_of_week")
        val KEY_ENABLE_SYSTEM_COLOR = booleanPreferencesKey("enable_system_color")
        val KEY_SELECTED_DARK_MODE = stringPreferencesKey("selected_dark_mode")
        val KEY_START_LOCALDATE = stringPreferencesKey("start_localdate")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_SEMESTER_YEAR_AND_NO = stringPreferencesKey("semester_year_and_no")
        val KEY_ENTER_UNIVERSITY_YEAR = stringPreferencesKey("enter_university_year")
        val KEY_IS_SCREENSHOT_MODE = booleanPreferencesKey("is_screenshot_mode")

        const val DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY = true
        const val DEFAULT_VALUE_IS_YEAR_DISPLAY = true
        const val DEFAULT_VALUE_IS_DATE_DISPLAY = true
        const val DEFAULT_VALUE_IS_TIME_DISPLAY = true
        const val DEFAULT_VALUE_COOKIES = "[]"
        const val DEFAULT_VALUE_IS_LOGIN = false
        const val DEFAULT_VALUE_DAY_OF_WEEK = -1
        const val DEFAULT_VALUE_DISPLAYED_WEEK = -1
        const val DEFAULT_VALUE_ENABLE_SYSTEM_COLOR = true
        const val DEFAULT_VALUE_SELECTED_DARK_MODE = "跟随系统"
        val DEFAULT_VALUE_START_LOCALDATE = LocalDate.now()
        const val DEFAULT_VALUE_USERNAME = ""
        const val DEFAULT_VALUE_PASSWORD = ""
        const val DEFAULT_VALUE_SEMESTER_YEAR_AND_NO = ""
        const val DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR = ""
        const val DEFAULT_VALUE_IS_SCREENSHOT_MODE = false
    }
}
