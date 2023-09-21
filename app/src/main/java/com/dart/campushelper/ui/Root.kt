package com.dart.campushelper.ui

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.data.AppContainer
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.AppDataStore.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.model.StudentInfo
import com.dart.campushelper.model.WeekHeader
import com.dart.campushelper.ui.grade.CreateFloatingActionButtonForGrade
import com.dart.campushelper.ui.grade.GradeScreen
import com.dart.campushelper.ui.grade.GradeViewModel
import com.dart.campushelper.ui.schedule.ScheduleScreen
import com.dart.campushelper.ui.schedule.ScheduleViewModel
import com.dart.campushelper.ui.settings.SettingsScreen
import com.dart.campushelper.ui.settings.SettingsViewModel
import com.dart.campushelper.ui.theme.CampusHelperTheme
import com.dart.campushelper.utils.Constants.Companion.STUDENT_INFO_URL
import com.dart.campushelper.utils.DefaultCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.time.LocalDate

data class Screen(val route: String, @StringRes val resourceId: Int)

class Root(
    private val appContainer: AppContainer
) {

    var cookies = emptyList<Cookie>()

    companion object {
        lateinit var scope: CoroutineScope
        lateinit var client: OkHttpClient
            private set
        lateinit var scheduleViewModel: ScheduleViewModel
            private set
        lateinit var gradeViewModel: GradeViewModel
            private set
        lateinit var settingsViewModel: SettingsViewModel
            private set
        var isLogin: Boolean? = null
            private set
        lateinit var username: String
            private set
        lateinit var semesterYearAndNo: String
            private set
        lateinit var enterUniversityYear: String
            private set
    }

    init {

        client = OkHttpClient().newBuilder()
            // Stop it from redirecting, to some extend, it decreases the network traffic
            // and improve the performance
            .followRedirects(false)
            // Auto handle cookies
            //.cookieJar(JavaNetCookieJar(CookieManager()))
            // Here we don't use the code above
            // Because storing the cookies to the setting area of the app
            // is the core function to make the app run forever without
            // more login actions in the future
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    val count = cookies.count { it.name == "rememberMe" }
                    if (url.toString() == "https://hbut.jw.chaoxing.com/admin/login" && count == 2) {
                        val changedCookies = cookies.map {
                            Cookie.Builder()
                                .name(it.name)
                                .value(it.value)
                                .domain(it.domain)
                                .path(it.path)
                                .expiresAt(Long.MAX_VALUE)
                                .build()
                        }
                        scope.launch {
                            App.container.appRepository.changeCookies(changedCookies.toSet())
                        }
                    }
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookies
                }
            })
            .build()
    }

    @SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Create() {

        scope = rememberCoroutineScope()

        isLogin =
            appContainer.appRepository.observeIsLogin().collectAsState(initial = null).value == true

        username =
            appContainer.appRepository.observeUsername()
                .collectAsState(initial = DEFAULT_VALUE_USERNAME).value

        semesterYearAndNo = appContainer.appRepository.observeSemesterYearAndNo().collectAsState(
            initial = DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
        ).value

        enterUniversityYear = appContainer.appRepository.observeEnterUniversityYear().collectAsState(
            initial = DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
        ).value

        scheduleViewModel = viewModel(
            factory = ScheduleViewModel.provideFactory(appContainer.appRepository)
        )
        settingsViewModel = viewModel(
            factory = SettingsViewModel.provideFactory(appContainer.appRepository)
        )
        gradeViewModel = viewModel(
            factory = GradeViewModel.provideFactory(appContainer.appRepository)
        )

        if (isLogin == false) {
            scope.launch {
                scheduleViewModel.courses.emit(emptyMap())
                gradeViewModel.grades.emit(emptyList())
            }
        }

        if (isLogin == true) {
            // Get student info
            client.newCall(
                Request.Builder()
                    .url(STUDENT_INFO_URL)
                    .post(FormBody.Builder().build())
                    .build()
            ).enqueue(
                DefaultCallback(
                    scope = scope,
                    actionWhenResponseSuccess = { response ->
                        val json = response.body!!.source().readString(Charset.defaultCharset())
                        val type: Type = object : TypeToken<StudentInfo?>() {}.type
                        val studentInfo: StudentInfo = Gson().fromJson(json, type)
                        val semesterYearAndNo = studentInfo.data?.records?.get(0)?.dataXnxq
                        val enterUniversityYear = studentInfo.data?.records?.get(0)?.rxnj
                        scope.launch {
                            if (semesterYearAndNo != null) {
                                App.container.appRepository.changeSemesterYearAndNo(semesterYearAndNo)
                            }
                            if (enterUniversityYear != null) {
                                App.container.appRepository.changeEnterUniversityYear(enterUniversityYear)
                            }
                        }
                    }
                )
            )
            // Get specific start date of the semester
            client.newCall(
                Request.Builder()
                    .url("https://hbut.jw.chaoxing.com/admin/getXqByZc")
                    .post(
                        FormBody.Builder()
                            .add("zc", "0")
                            .build()
                    ).build()
            ).enqueue(
                DefaultCallback(
                    scope = scope,
                    actionWhenResponseSuccess = { response ->
                        val json = response.body!!.source().readString(Charset.defaultCharset())
                        val type: Type = object : TypeToken<WeekHeader?>() {}.type
                        val weekHeader: WeekHeader = Gson().fromJson(json, type)
                        weekHeader.data.first().date?.let {
                            scope.launch {
                                val year = LocalDate.now().year.toString()
                                var month = it.split("-")[0]
                                if (month.length == 1) {
                                    month = "0$month"
                                }
                                var day = it.split("-")[1]
                                if (day.length == 1) {
                                    day = "0$day"
                                }
                                val formattedDate = "$year-$month-$day"
                                val deltaWeekCount: Int =
                                    (LocalDate.now().dayOfYear - LocalDate.parse(formattedDate).dayOfYear) / 7
                                App.container.appRepository.changeStartLocalDate(
                                    LocalDate.parse(
                                        formattedDate
                                    )
                                )
                                App.container.appRepository.changeCurrentWeek(deltaWeekCount)
                            }
                        }
                        scheduleViewModel.loadCourses()
                        gradeViewModel.loadGrades()
                        gradeViewModel.loadRankingInfo()
                    }
                )
            )
        }

        scope.launch {
            App.container.appRepository.observeCookies().collect() {
                cookies = it.toList()
            }
        }

        val schedule = Screen("课表", R.string.schedule_label)
        val grade = Screen("成绩", R.string.grade_label)
        val settings = Screen("设置", R.string.settings_label)

        val items = listOf(
            schedule,
            grade,
            settings
        )
        val idsOutline = listOf(
            rememberViewTimeline(),
            rememberDriveFileRenameOutline(),
            rememberSettings()
        )
        val idsFill = listOf(
            rememberViewTimelineFilled(),
            rememberDriveFileRenameOutlineFilled(),
            rememberSettingsFilled()
        )

        CampusHelperTheme(appContainer) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                var showActions by remember { mutableStateOf(true) }
                var title by remember { mutableStateOf("") }

                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = currentDestination?.route ?: "",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (currentDestination?.route == schedule.route && isLogin == true) {
                                        Text(
                                            text = "当前第 ${scheduleViewModel.currentWeek.collectAsState().value} 周",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.align(Alignment.Bottom)
                                        )
                                    }
                                }
                            },
                            actions = {
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, screen ->
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    icon = {
                                        Icon(
                                            imageVector = when (currentDestination?.route == screen.route) {
                                                true -> idsFill[index]
                                                false -> idsOutline[index]
                                            },
                                            contentDescription = screen.route
                                        )
                                    },
                                    label = {
                                        Text(
                                            screen.route
                                        )
                                    },
                                    alwaysShowLabel = false,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(0) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = MainActivity.snackBarHostState)
                    },
                    floatingActionButton = {
                        if (isLogin == true) {
                            when (currentDestination?.route) {
                                grade.route -> CreateFloatingActionButtonForGrade()
                                else -> null
                            }
                        }
                    }
                ) {
                    NavHost(
                        navController,
                        schedule.route,
                        Modifier.padding(it),
                    ) {
                        composable(
                            schedule.route,
                        ) {
                            showActions = true
                            ScheduleScreen(scheduleViewModel)
                        }
                        composable(
                            grade.route,
                        ) {
                            title = "成绩"
                            showActions = true
                            GradeScreen(gradeViewModel)
                        }
                        composable(
                            settings.route,
                        ) {

                            title = "设置"
                            showActions = false
                            SettingsScreen(settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}


