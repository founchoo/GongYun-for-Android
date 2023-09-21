package com.dart.campushelper.data.app.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dart.campushelper.data.AppDataStore
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_CURRENT_WEEK
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_DAY_OF_WEEK
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_START_LOCALDATE
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.data.AppDataStore.Companion.KEY_CURRENT_WEEK
import com.dart.campushelper.data.AppDataStore.Companion.KEY_DAY_OF_WEEK
import com.dart.campushelper.data.AppDataStore.Companion.KEY_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.AppDataStore.Companion.KEY_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.AppDataStore.Companion.KEY_IS_LOGIN
import com.dart.campushelper.data.AppDataStore.Companion.KEY_IS_PIN
import com.dart.campushelper.data.AppDataStore.Companion.KEY_SELECTED_DARK_MODE
import com.dart.campushelper.data.AppDataStore.Companion.KEY_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.AppDataStore.Companion.KEY_START_LOCALDATE
import com.dart.campushelper.data.AppDataStore.Companion.KEY_USERNAME
import com.dart.campushelper.data.AppDataStore.Companion.dataStore
import com.dart.campushelper.data.app.AppRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import java.time.LocalDate

/**
 * Implementation of InterestRepository that returns a hardcoded list of
 * topics, people and publications synchronously.
 */
class AppRepository(
    val context: Context
) : AppRepository {

    private val dataStore = context.dataStore

    // for now, keep the selections in memory

    // Used to make suspend functions that read and update state safe to call from any thread
    private val mutex = Mutex()

    override suspend fun changeIsLogin(isLogin: Boolean) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_IS_LOGIN] = isLogin
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

    override suspend fun changeCurrentWeek(week: Int) {
        mutex.withLock {
            dataStore.edit {
                it[KEY_CURRENT_WEEK] = week
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

    override suspend fun changeCookies(cookies: Set<Cookie>) {
        mutex.withLock {
            dataStore.edit {
                it[AppDataStore.KEY_COOKIES] = Gson().toJson(cookies)
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

    override fun observeIsLogin(): Flow<Boolean?> = dataStore.data.map {
        it[KEY_IS_LOGIN]
    }

    override fun observeSelectedDarkMode(): Flow<String> = dataStore.data.map { it[KEY_SELECTED_DARK_MODE] ?: DEFAULT_VALUE_SELECTED_DARK_MODE }

    override fun observeStartLocalDate(): Flow<LocalDate?> {
        return dataStore.data.map {
            val localDateString = it[KEY_START_LOCALDATE] ?: DEFAULT_VALUE_START_LOCALDATE
            if (localDateString == DEFAULT_VALUE_START_LOCALDATE) {
                null
            } else {
                LocalDate.parse(localDateString)
            }
        }
    }

    override fun observeCurrentWeek(): Flow<Int> = dataStore.data.map { it[KEY_CURRENT_WEEK] ?: DEFAULT_VALUE_CURRENT_WEEK }

    override fun observeDayOfWeek(): Flow<Int> = dataStore.data.map { it[KEY_DAY_OF_WEEK] ?: DEFAULT_VALUE_DAY_OF_WEEK }

    override fun observeEnableSystemColor(): Flow<Boolean?> = dataStore.data.map {
        it[KEY_ENABLE_SYSTEM_COLOR]
    }

    override fun observeCookies(): Flow<Set<Cookie>> = dataStore.data.map {
        val json = it[AppDataStore.KEY_COOKIES] ?: AppDataStore.DEFAULT_VALUE_COOKIES
        if (json == AppDataStore.DEFAULT_VALUE_COOKIES) {
            emptySet<Cookie>()
        } else {
            val typeOfT = object : TypeToken<Set<Cookie>>() {}.type
            Gson().fromJson(json, typeOfT)
        }
    }

    override fun observeIsPin(): Flow<Boolean?> = dataStore.data.map {
        it[KEY_IS_PIN]
    }

    override fun observeUsername(): Flow<String> = dataStore.data.map {
        it[KEY_USERNAME] ?: DEFAULT_VALUE_USERNAME
    }

    override fun observeSemesterYearAndNo(): Flow<String> = dataStore.data.map {
        it[KEY_SEMESTER_YEAR_AND_NO] ?: DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
    }

    override fun observeEnterUniversityYear(): Flow<String> = dataStore.data.map {
        it[KEY_ENTER_UNIVERSITY_YEAR] ?: DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
    }
}
