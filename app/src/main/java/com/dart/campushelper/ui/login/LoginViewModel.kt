package com.dart.campushelper.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_CURRENT_WEEK
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_IS_LOGIN
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.data.app.AppRepository
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.ui.Root
import com.dart.campushelper.utils.Constants.Companion.LOGIN_URL
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDate

class LoginViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    var isShowLoginDialog by mutableStateOf(false)
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    fun login() {

        Root.client.newCall(
            Request.Builder()
                .url(LOGIN_URL)
                .post(
                    FormBody.Builder()
                        .add("username", username)
                        .add("password", password)
                        .add("jcaptchaCode", "")
                        .add("rememberMe", "1")
                        .build()
                )
                .build()
        ).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                viewModelScope.launch {
                    MainActivity.snackBarHostState.showSnackbar("登录失败")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // When login successfully, the server will redirect to home page
                // But we already disable it from redirecting in App.kt
                // So here, it will return 302 and we can detect it
                if (response.code == 302) {
                    viewModelScope.launch {
                        appRepository.changeIsLogin(true)
                        appRepository.changeUsername(username)
                        MainActivity.snackBarHostState.showSnackbar("登录成功")
                    }
                    isShowLoginDialog = false
                } else {
                    viewModelScope.launch {
                        appRepository.changeIsLogin(false)
                        MainActivity.snackBarHostState.showSnackbar("登录失败")
                    }
                }
            }
        })
    }

    fun logout() {
        viewModelScope.launch {
            appRepository.changeCurrentWeek(DEFAULT_VALUE_CURRENT_WEEK)
            appRepository.changeCookies(emptySet())
            appRepository.changeStartLocalDate(LocalDate.now())
            appRepository.changeUsername(DEFAULT_VALUE_USERNAME)
            appRepository.changeIsLogin(DEFAULT_VALUE_IS_LOGIN)
            appRepository.changeSemesterYearAndNo(DEFAULT_VALUE_SEMESTER_YEAR_AND_NO)
            appRepository.changeEnterUniversityYear(DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR)
        }
    }

    companion object {
        fun provideFactory(
            appRepository: AppRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(appRepository) as T
            }
        }
    }
}