package com.dart.campushelper.ui

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.UiModeManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dart.campushelper.R
import com.dart.campushelper.receiver.AppWidgetPinnedReceiver
import com.dart.campushelper.receiver.DateChangeReceiver
import com.dart.campushelper.ui.grade.CreateFloatingActionButtonForGrade
import com.dart.campushelper.ui.grade.GradeScreen
import com.dart.campushelper.ui.grade.GradeViewModel
import com.dart.campushelper.ui.login.LoginViewModel
import com.dart.campushelper.ui.schedule.CreateActionsForSchedule
import com.dart.campushelper.ui.schedule.ScheduleScreen
import com.dart.campushelper.ui.schedule.ScheduleViewModel
import com.dart.campushelper.ui.settings.SettingsScreen
import com.dart.campushelper.ui.settings.SettingsViewModel
import com.dart.campushelper.ui.theme.CampusHelperTheme
import com.dart.campushelper.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class Screen(val route: String, @StringRes val resourceId: Int)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    private val loginViewModel: LoginViewModel by viewModels()

    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val gradeViewModel: GradeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val mainViewModel: MainViewModel by viewModels()

    private val mDateReceiver = DateChangeReceiver()

    companion object {
        lateinit var scope: CoroutineScope
        lateinit var snackBarHostState: SnackbarHostState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager.setApplicationNightMode(
                when (themeViewModel.uiState.value.darkMode) {
                    "开启" -> UiModeManager.MODE_NIGHT_YES
                    "关闭" -> UiModeManager.MODE_NIGHT_NO
                    else -> UiModeManager.MODE_NIGHT_AUTO
                }
            )
        }

        setContent {
            scope = rememberCoroutineScope()
            snackBarHostState = remember { SnackbarHostState() }
            AddBackHandler()
            CampusHelperApp(mainViewModel)
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CampusHelperApp(mainViewModel: MainViewModel) {

        val mainUiState by mainViewModel.uiState.collectAsState()
        val scheduleUiState by scheduleViewModel.uiState.collectAsState()

        val schedule = Screen("课表", R.string.schedule_label)
        val grade = Screen("成绩", R.string.grade_label)
        val settings = Screen("设置", R.string.settings_label)

        val idsOutline = mapOf(
            schedule to rememberViewTimeline(),
            grade to rememberDriveFileRenameOutline(),
            settings to rememberSettings()
        )
        val idsFill = mapOf(
            schedule to rememberViewTimelineFilled(),
            grade to rememberDriveFileRenameOutlineFilled(),
            settings to rememberSettingsFilled(),
        )

        CampusHelperTheme(themeViewModel) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                var showActions by remember { mutableStateOf(true) }

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
                                    if (currentDestination?.route == schedule.route) {
                                        Text(
                                            text = "当前第 ${scheduleUiState.currentWeek} 周，正在浏览第 ${scheduleUiState.browsedWeek} 周",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.align(Alignment.Bottom)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentDestination?.route == schedule.route) {
                                    CreateActionsForSchedule(
                                        scheduleViewModel,
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            (if (mainUiState.isLogin)
                                listOf(
                                    schedule,
                                    grade,
                                    settings
                                ) else
                                listOf(settings)).forEachIndexed { index, screen ->
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    icon = {
                                        Icon(
                                            imageVector = when (currentDestination?.route == screen.route) {
                                                true -> idsFill[screen]!!
                                                false -> idsOutline[screen]!!
                                            },
                                            contentDescription = screen.route
                                        )
                                    },
                                    label = { Text(screen.route) },
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
                        SnackbarHost(hostState = snackBarHostState)
                    },
                    floatingActionButton = {
                        if (mainUiState.isLogin) {
                            when (currentDestination?.route) {
                                grade.route -> CreateFloatingActionButtonForGrade()
                                else -> null
                            }
                        }
                    }
                ) {
                    NavHost(
                        navController,
                        if (mainUiState.isLogin) schedule.route else settings.route,
                        Modifier.padding(it),
                    ) {
                        composable(
                            schedule.route,
                        ) {
                            ScheduleScreen(scheduleViewModel)
                        }
                        composable(
                            grade.route,
                        ) {
                            GradeScreen(gradeViewModel)
                        }
                        composable(
                            settings.route,
                        ) {
                            SettingsScreen(settingsViewModel, loginViewModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AddBackHandler() {
        val scope = rememberCoroutineScope()
        var showExitHint by remember { mutableStateOf(false) }
        BackHandler(true) {
            if (showExitHint) {
                scope.launch {
                    ActivityCompat.finishAffinity(this@MainActivity)
                }
                return@BackHandler
            }
            showExitHint = true
            scope.launch {
                showExitHint =
                    snackBarHostState.showSnackbar("再按一次以退出") != SnackbarResult.Dismissed
            }
        }
    }

    override fun onResume() {
        registerReceiver(mDateReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(mDateReceiver)
        super.onPause()
    }
}

/**
 * Extension method to request the launcher to pin the given AppWidgetProviderInfo
 *
 * Note: the optional success callback to retrieve if the widget was placed might be unreliable
 * depending on the default launcher implementation. Also, it does not callback if user cancels the
 * request.
 */
fun AppWidgetProviderInfo.pin(context: Context) {
    val successCallback = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AppWidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
}

@Composable
private fun PinUnavailableBanner() {
    androidx.compose.material.Text(
        text = stringResource(
            id = R.string.placeholder_main_activity_pin_unavailable
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.error)
            .padding(16.dp),
        color = MaterialTheme.colors.onError
    )
}
