package com.dart.campushelper.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

class AppDataStore {

    companion object {

        val Context.dataStore by preferencesDataStore(
            name = "app_preferences"
        )

        val KEY_IS_LOGIN = booleanPreferencesKey("is_login")
        val KEY_DAY_OF_WEEK = intPreferencesKey("day_of_week")
        val KEY_CURRENT_WEEK = intPreferencesKey("selected_week")
        val KEY_ENABLE_SYSTEM_COLOR = booleanPreferencesKey("enable_system_color")
        val KEY_SELECTED_DARK_MODE = stringPreferencesKey("selected_dark_mode")
        val KEY_START_LOCALDATE = stringPreferencesKey("start_localdate")
        val KEY_COOKIES = stringPreferencesKey("cookies")
        val KEY_IS_PIN = booleanPreferencesKey("is_pin")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_SEMESTER_YEAR_AND_NO = stringPreferencesKey("semester_year_and_no")
        val KEY_ENTER_UNIVERSITY_YEAR = stringPreferencesKey("enter_university_year")

        val DEFAULT_VALUE_IS_LOGIN = false
        val DEFAULT_VALUE_DAY_OF_WEEK = -1
        val DEFAULT_VALUE_CURRENT_WEEK = -1
        val DEFAULT_VALUE_DISPLAYED_WEEK = -1
        val DEFAULT_VALUE_ENABLE_SYSTEM_COLOR = false
        val DEFAULT_VALUE_SELECTED_DARK_MODE = "跟随系统"
        val DEFAULT_VALUE_START_LOCALDATE = ""
        val DEFAULT_VALUE_COOKIES = ""
        val DEFAULT_VALUE_IS_PIN = false
        val DEFAULT_VALUE_USERNAME = ""
        val DEFAULT_VALUE_SEMESTER_YEAR_AND_NO = ""
        val DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR = ""
    }
}
