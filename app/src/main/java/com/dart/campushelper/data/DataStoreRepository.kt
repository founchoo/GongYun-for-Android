package com.dart.campushelper.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dart.campushelper.api.DataStoreService
import com.dart.campushelper.data.KEYS.KEY_COOKIES
import com.dart.campushelper.data.KEYS.KEY_DAY_OF_WEEK
import com.dart.campushelper.data.KEYS.KEY_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.KEYS.KEY_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.KEYS.KEY_IS_PIN
import com.dart.campushelper.data.KEYS.KEY_PASSWORD
import com.dart.campushelper.data.KEYS.KEY_SELECTED_DARK_MODE
import com.dart.campushelper.data.KEYS.KEY_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.KEYS.KEY_START_LOCALDATE
import com.dart.campushelper.data.KEYS.KEY_USERNAME
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_COOKIES
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_DAY_OF_WEEK
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_PASSWORD
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_USERNAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import java.time.LocalDate
import javax.inject.Inject

/**
 * Implementation of InterestRepository that returns a hardcoded list of
 * topics, people and publications synchronously.
 */
class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DataStoreService {

    // Used to make suspend functions that read and update state safe to call from any thread
    private val mutex = Mutex()

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
                it[KEYS.KEY_IS_LOGIN] = isLogin
            }
        }
    }

    override suspend fun changeSelectedDarkMode(darkMode: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_SELECTED_DARK_MODE] = darkMode
            }
        }
    }

    override suspend fun changeStartLocalDate(localDate: LocalDate) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_START_LOCALDATE] = localDate.toString()
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

    override suspend fun changeIsPin(isPin: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_PIN] = isPin
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

    override suspend fun changeSemesterYearAndNo(semesterYearAndNo: String) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_SEMESTER_YEAR_AND_NO] = semesterYearAndNo
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
        it[KEYS.KEY_IS_LOGIN] ?: VALUES.DEFAULT_VALUE_IS_LOGIN
    }

    override fun observeSelectedDarkMode(): Flow<String> =
        dataStore.data.map { it[KEY_SELECTED_DARK_MODE] ?: DEFAULT_VALUE_SELECTED_DARK_MODE }

    override fun observeStartLocalDate(): Flow<LocalDate?> = dataStore.data.map {
        LocalDate.parse(it[KEY_START_LOCALDATE] ?: LocalDate.now().toString())
    }

    override fun observeDayOfWeek(): Flow<Int> =
        dataStore.data.map { it[KEY_DAY_OF_WEEK] ?: DEFAULT_VALUE_DAY_OF_WEEK }

    override fun observeEnableSystemColor(): Flow<Boolean> = dataStore.data.map {
        it[KEY_ENABLE_SYSTEM_COLOR] ?: false
    }

    override fun observeIsPin(): Flow<Boolean?> = dataStore.data.map {
        it[KEY_IS_PIN]
    }

    override fun observeUsername(): Flow<String> = dataStore.data.map {
        it[KEY_USERNAME] ?: DEFAULT_VALUE_USERNAME
    }

    override fun observePassword(): Flow<String> = dataStore.data.map {
        it[KEY_PASSWORD] ?: DEFAULT_VALUE_PASSWORD
    }

    override fun observeSemesterYearAndNo(): Flow<String> = dataStore.data.map {
        it[KEY_SEMESTER_YEAR_AND_NO] ?: DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
    }

    override fun observeEnterUniversityYear(): Flow<String> = dataStore.data.map {
        it[KEY_ENTER_UNIVERSITY_YEAR] ?: DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
    }

    companion object {
        const val PREFS_NAME = "user_datastore"
    }
}

object KEYS {

    val KEY_COOKIES = stringPreferencesKey("cookies")
    val KEY_IS_LOGIN = booleanPreferencesKey("is_login")
    val KEY_DAY_OF_WEEK = intPreferencesKey("day_of_week")
    val KEY_ENABLE_SYSTEM_COLOR = booleanPreferencesKey("enable_system_color")
    val KEY_SELECTED_DARK_MODE = stringPreferencesKey("selected_dark_mode")
    val KEY_START_LOCALDATE = stringPreferencesKey("start_localdate")
    val KEY_IS_PIN = booleanPreferencesKey("is_pin")
    val KEY_USERNAME = stringPreferencesKey("username")
    val KEY_PASSWORD = stringPreferencesKey("password")
    val KEY_SEMESTER_YEAR_AND_NO = stringPreferencesKey("semester_year_and_no")
    val KEY_ENTER_UNIVERSITY_YEAR = stringPreferencesKey("enter_university_year")
}

object VALUES {
    val DEFAULT_VALUE_COOKIES = "[]"
    val DEFAULT_VALUE_IS_LOGIN = false
    val DEFAULT_VALUE_DAY_OF_WEEK = -1
    val DEFAULT_VALUE_DISPLAYED_WEEK = -1
    val DEFAULT_VALUE_ENABLE_SYSTEM_COLOR = false
    val DEFAULT_VALUE_SELECTED_DARK_MODE = "跟随系统"
    val DEFAULT_VALUE_START_LOCALDATE = LocalDate.now()
    val DEFAULT_VALUE_IS_PIN = false
    val DEFAULT_VALUE_USERNAME = ""
    val DEFAULT_VALUE_PASSWORD = ""
    val DEFAULT_VALUE_SEMESTER_YEAR_AND_NO = ""
    val DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR = ""
}
