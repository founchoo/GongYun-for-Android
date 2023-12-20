package com.dart.campushelper.repo

import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.api.DataStoreService
import com.dart.campushelper.viewmodel.DarkMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.osipxd.security.crypto.createEncrypted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepo @Inject constructor() : DataStoreService {

    // Used to make suspend functions that read and update state safe to call from any thread
    private val mutex = Mutex()

    private val dataStore = PreferenceDataStoreFactory.createEncrypted {
        EncryptedFile.Builder(
            context.dataStoreFile(PREFS_NAME),
            context,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    override suspend fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_OTHER_COURSE_DISPLAY] = isOtherCourseDisplay
            }
        }
    }

    override suspend fun changeIsYearDisplay(isYearDisplay: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_YEAR_DISPLAY] = isYearDisplay
            }
        }
    }

    override suspend fun changeIsDateDisplay(isDateDisplay: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_DATE_DISPLAY] = isDateDisplay
            }
        }
    }

    override suspend fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_TIME_DISPLAY] = isTimeDisplay
            }
        }
    }

    override suspend fun changeCookies(cookies: List<Cookie>) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_COOKIES] = Gson().toJson(cookies)
            }
        }
    }

    override suspend fun changeIsLogin(isLogin: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_LOGIN] = isLogin
            }
        }
    }

    override suspend fun changeSelectedDarkMode(darkMode: Int) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_SELECTED_DARK_MODE] = darkMode
            }
        }
    }

    override suspend fun changeEnableSystemColor(enable: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_ENABLE_SYSTEM_COLOR] = enable
            }
        }
    }

    override suspend fun changeUsername(username: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_USERNAME] = username
            }
        }
    }

    override suspend fun changePassword(password: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_PASSWORD] = password
            }
        }
    }

    override suspend fun changeSemesterYearAndNo(yearAndSemester: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_SEMESTER_YEAR_AND_NO] = yearAndSemester
            }
        }
    }

    override suspend fun changeEnterUniversityYear(enterUniversityYear: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_ENTER_UNIVERSITY_YEAR] = enterUniversityYear
            }
        }
    }

    override suspend fun changeIsScreenshotMode(isScreenshotMode: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_SCREENSHOT_MODE] = isScreenshotMode
            }
        }
    }

    override suspend fun changeIsLessonReminderEnabled(isLessonReminderEnabled: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_LESSON_REMINDER_ENABLED] = isLessonReminderEnabled
            }
        }
    }

    override fun observeIsOtherCourseDisplay(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_OTHER_COURSE_DISPLAY] ?: DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
    }

    override fun observeIsYearDisplay(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_YEAR_DISPLAY] ?: DEFAULT_VALUE_IS_YEAR_DISPLAY
    }

    override fun observeIsDateDisplay(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_DATE_DISPLAY] ?: DEFAULT_VALUE_IS_DATE_DISPLAY
    }

    override fun observeIsTimeDisplay(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_TIME_DISPLAY] ?: DEFAULT_VALUE_IS_TIME_DISPLAY
    }

    override fun observeCookies(): Flow<List<Cookie>> = dataStore.data.map {
        val json = it[KEY_COOKIES] ?: DEFAULT_VALUE_COOKIES
        if (json == DEFAULT_VALUE_COOKIES) {
            emptyList<Cookie>()
        } else {
            val typeOfT = object : TypeToken<List<Cookie>>() {}.type
            Gson().fromJson(json, typeOfT)
        }
    }

    override fun observeIsLogin(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_LOGIN] ?: DEFAULT_VALUE_IS_LOGIN
    }

    override fun observeSelectedDarkMode(): Flow<Int> =
        dataStore.data.map {
            it[KEY_SELECTED_DARK_MODE] ?: DEFAULT_VALUE_SELECTED_DARK_MODE
        }

    override fun observeDayOfWeek(): Flow<Int> =
        dataStore.data.map { it[KEY_DAY_OF_WEEK] ?: DEFAULT_VALUE_DAY_OF_WEEK }

    override fun observeEnableSystemColor(): Flow<Boolean> = dataStore.data.map {
        it[KEY_ENABLE_SYSTEM_COLOR] ?: DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
    }

    override fun observeUsername(): Flow<String> = dataStore.data.map {
        it[KEY_USERNAME] ?: DEFAULT_VALUE_USERNAME
    }

    override fun observePassword(): Flow<String> = dataStore.data.map {
        it[KEY_PASSWORD] ?: DEFAULT_VALUE_PASSWORD
    }

    override fun observeYearAndSemester(): Flow<String> = dataStore.data.map {
        it[KEY_SEMESTER_YEAR_AND_NO] ?: DEFAULT_VALUE_YEAR_AND_SEMESTER
    }

    override fun observeEnterUniversityYear(): Flow<String> = dataStore.data.map {
        it[KEY_ENTER_UNIVERSITY_YEAR] ?: DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
    }

    override fun observeIsScreenshotMode(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_SCREENSHOT_MODE] ?: DEFAULT_VALUE_IS_SCREENSHOT_MODE
    }

    override fun observeIsLessonReminderEnabled(): Flow<Boolean> = dataStore.data.map {
        it[KEY_IS_LESSON_REMINDER_ENABLED] ?: DEFAULT_VALUE_IS_LESSON_REMINDER_ENABLED
    }

    companion object {
        const val PREFS_NAME = "user_datastore.preferences_pb"

        val KEY_IS_OTHER_COURSE_DISPLAY = booleanPreferencesKey("is_other_course_display")
        val KEY_IS_YEAR_DISPLAY = booleanPreferencesKey("is_year_display")
        val KEY_IS_DATE_DISPLAY = booleanPreferencesKey("is_date_display")
        val KEY_IS_TIME_DISPLAY = booleanPreferencesKey("is_time_display")
        val KEY_COOKIES = stringPreferencesKey("cookies")
        val KEY_IS_LOGIN = booleanPreferencesKey("is_login")
        val KEY_DAY_OF_WEEK = intPreferencesKey("day_of_week")
        val KEY_ENABLE_SYSTEM_COLOR = booleanPreferencesKey("enable_system_color")
        val KEY_SELECTED_DARK_MODE = intPreferencesKey("selected_dark_mode")
        val KEY_START_LOCALDATE = stringPreferencesKey("start_localdate")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_SEMESTER_YEAR_AND_NO = stringPreferencesKey("semester_year_and_no")
        val KEY_ENTER_UNIVERSITY_YEAR = stringPreferencesKey("enter_university_year")
        val KEY_IS_SCREENSHOT_MODE = booleanPreferencesKey("is_screenshot_mode")
        val KEY_IS_LESSON_REMINDER_ENABLED = booleanPreferencesKey("is_lesson_reminder_enabled")

        const val DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY = true
        const val DEFAULT_VALUE_IS_YEAR_DISPLAY = true
        const val DEFAULT_VALUE_IS_DATE_DISPLAY = true
        const val DEFAULT_VALUE_IS_TIME_DISPLAY = true
        const val DEFAULT_VALUE_COOKIES = "[]"
        const val DEFAULT_VALUE_IS_LOGIN = false
        const val DEFAULT_VALUE_DAY_OF_WEEK = -1
        const val DEFAULT_VALUE_ENABLE_SYSTEM_COLOR = true
        val DEFAULT_VALUE_SELECTED_DARK_MODE = DarkMode.SYSTEM.ordinal
        const val DEFAULT_VALUE_USERNAME = ""
        const val MOCK_VALUE_USERNAME = "MOCK_USERNAME"
        const val DEFAULT_VALUE_PASSWORD = ""
        const val MOCK_VALUE_PASSWORD = "MOCK_PASSWORD"
        const val DEFAULT_VALUE_YEAR_AND_SEMESTER = ""
        val MOCK_VALUE_YEAR_AND_SEMESTER = LocalDate.now().year.let { "${it}-${it + 1}-1" }
        const val DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR = ""
        val MOCK_VALUE_ENTER_UNIVERSITY_YEAR = "${LocalDate.now().year - 3}"
        const val DEFAULT_VALUE_IS_SCREENSHOT_MODE = false
        const val DEFAULT_VALUE_IS_LESSON_REMINDER_ENABLED = false
    }
}
