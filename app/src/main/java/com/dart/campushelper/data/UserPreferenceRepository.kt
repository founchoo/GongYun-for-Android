package com.dart.campushelper.data

import com.dart.campushelper.api.DataStoreService
import com.dart.campushelper.api.UserPreferenceService
import kotlinx.coroutines.flow.Flow
import okhttp3.Cookie
import java.time.LocalDate
import javax.inject.Inject

class UserPreferenceRepository @Inject constructor(
    private val dataStoreService: DataStoreService,
): UserPreferenceService {

    override suspend fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean) {
        dataStoreService.changeIsOtherCourseDisplay(isOtherCourseDisplay)
    }

    override suspend fun changeIsYearDisplay(isYearDisplay: Boolean) {
        dataStoreService.changeIsYearDisplay(isYearDisplay)
    }

    override suspend fun changeIsDateDisplay(isDateDisplay: Boolean) {
        dataStoreService.changeIsDateDisplay(isDateDisplay)
    }

    override suspend fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        dataStoreService.changeIsTimeDisplay(isTimeDisplay)
    }

    override suspend fun changeCookies(cookies: List<Cookie>) {
        dataStoreService.changeCookies(cookies)
    }

    override suspend fun changeIsLogin(isLogin: Boolean) {
        dataStoreService.changeIsLogin(isLogin)
    }

    override suspend fun changeSelectedDarkMode(darkMode: String) {
        dataStoreService.changeSelectedDarkMode(darkMode)
    }

    override suspend fun changeStartLocalDate(localDate: LocalDate) {
        dataStoreService.changeStartLocalDate(localDate)
    }

    override suspend fun changeEnableSystemColor(enable: Boolean) {
        dataStoreService.changeEnableSystemColor(enable)
    }

    override suspend fun changeIsPin(isPin: Boolean) {
        dataStoreService.changeIsPin(isPin)
    }

    override suspend fun changeUsername(username: String) {
        dataStoreService.changeUsername(username)
    }

    override suspend fun changePassword(password: String) {
        dataStoreService.changePassword(password)
    }

    override suspend fun changeSemesterYearAndNo(semesterYearAndNo: String) {
        dataStoreService.changeSemesterYearAndNo(semesterYearAndNo)
    }

    override suspend fun changeEnterUniversityYear(enterUniversityYear: String) {
        dataStoreService.changeEnterUniversityYear(enterUniversityYear)
    }

    /**
     * Observe section.
     */
    override fun observeIsOtherCourseDisplay(): Flow<Boolean> {
        return dataStoreService.observeIsOtherCourseDisplay()
    }

    override fun observeIsYearDisplay(): Flow<Boolean> {
        return dataStoreService.observeIsYearDisplay()
    }

    override fun observeIsDateDisplay(): Flow<Boolean> {
        return dataStoreService.observeIsDateDisplay()
    }

    override fun observeIsTimeDisplay(): Flow<Boolean> {
        return dataStoreService.observeIsTimeDisplay()
    }

    override fun observeCookies(): Flow<List<Cookie>> {
        return dataStoreService.observeCookies()
    }

    override fun observeIsLogin(): Flow<Boolean> {
        return dataStoreService.observeIsLogin()
    }

    override fun observeSelectedDarkMode(): Flow<String> {
        return dataStoreService.observeSelectedDarkMode()
    }

    override fun observeStartLocalDate(): Flow<LocalDate?> {
        return dataStoreService.observeStartLocalDate()
    }

    override fun observeDayOfWeek(): Flow<Int> {
        return dataStoreService.observeDayOfWeek()
    }

    override fun observeEnableSystemColor(): Flow<Boolean?> {
        return dataStoreService.observeEnableSystemColor()
    }

    override fun observeIsPin(): Flow<Boolean> {
        return dataStoreService.observeIsPin()
    }

    override fun observeUsername(): Flow<String> {
        return dataStoreService.observeUsername()
    }

    override fun observePassword(): Flow<String> {
        return dataStoreService.observePassword()
    }

    override fun observeSemesterYearAndNo(): Flow<String> {
        return dataStoreService.observeSemesterYearAndNo()
    }

    override fun observeEnterUniversityYear(): Flow<String> {
        return dataStoreService.observeEnterUniversityYear()
    }
}