package com.dart.campushelper.ui.settings

import android.appwidget.AppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.App
import com.dart.campushelper.data.AppDataStore
import com.dart.campushelper.data.app.AppRepository
import com.dart.campushelper.ui.pin
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    val isLogin = appRepository.observeIsLogin().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_IS_LOGIN
    )

    val username = appRepository.observeUsername().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_USERNAME
    )

    val enableSystemColor = appRepository.observeEnableSystemColor().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
    )

    val selectedDarkModel = appRepository.observeSelectedDarkMode().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_SELECTED_DARK_MODE
    )

    val isPin = appRepository.observeIsPin().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_IS_PIN
    )

    fun changeEnableSystemColor(enable: Boolean) {
        viewModelScope.launch {
            appRepository.changeEnableSystemColor(enable)
        }
    }

    fun changeSelectedDarkMode(darkMode: String) {
        viewModelScope.launch {
            appRepository.changeSelectedDarkMode(darkMode)
        }
    }

    fun changeIsPin(isPin: Boolean) {
        viewModelScope.launch {
            appRepository.changeIsPin(isPin)
        }
        if (isPin) {
            val widgetManager = AppWidgetManager.getInstance(App.context)
            // Get a list of our app widget providers to retrieve their info
            val widgetProviders =
                widgetManager.getInstalledProvidersForPackage(App.context.packageName, null)
            widgetProviders[0].pin(App.context)
        } else {
        }
    }

    companion object {
        fun provideFactory(
            appRepository: AppRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(appRepository) as T
            }
        }
    }
}
