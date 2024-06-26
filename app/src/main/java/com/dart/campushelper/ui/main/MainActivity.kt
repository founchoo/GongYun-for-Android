package com.dart.campushelper.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.NoBorderTextField
import com.dart.campushelper.ui.grade.ActionsForGrade
import com.dart.campushelper.ui.grade.FloatingActionButtonForGrade
import com.dart.campushelper.ui.grade.GradeScreen
import com.dart.campushelper.ui.schedule.ActionsForSchedule
import com.dart.campushelper.ui.schedule.ScheduleScreen
import com.dart.campushelper.ui.settings.SettingsScreen
import com.dart.campushelper.ui.theme.CampusHelperTheme
import com.dart.campushelper.utils.AcademicYearAndSemester
import com.dart.campushelper.utils.Permission.Companion.PERMISSION_REQUEST_CODE
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import com.dart.campushelper.viewmodel.LoginViewModel
import com.dart.campushelper.viewmodel.MainUiState
import com.dart.campushelper.viewmodel.MainViewModel
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import com.dart.campushelper.viewmodel.SettingsViewModel
import com.dart.campushelper.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

lateinit var focusSearchBarRequester: FocusRequester

enum class Route {
    SCHEDULE,
    GRADE,
    SETTINGS
}

data class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val enabled: Boolean,
    val title: @Composable (() -> Unit) = {
        Text(
            text = stringResource(resourceId),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    val fab: @Composable (() -> Unit)? = null,
    val actions: @Composable (() -> Unit)? = null,
    val content: @Composable () -> Unit,
)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val gradeViewModel: GradeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var mainUiState: MainUiState
    private lateinit var scheduleUiState: ScheduleUiState
    private lateinit var gradeUiState: GradeUiState

    private lateinit var screens: Map<Route, Screen>

    private lateinit var navHostController: NavHostController
    private var currentDestination: NavDestination? = null

    companion object {
        lateinit var snackBarHostState: SnackbarHostState
        lateinit var mainActivity: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivity = this

        setContent {
            snackBarHostState = remember { SnackbarHostState() }
            focusSearchBarRequester = remember { FocusRequester() }
            CampusHelperApp()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0] == Manifest.permission.POST_NOTIFICATIONS) {
                    settingsViewModel.changeIsLessonReminderEnabled(true)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CampusHelperApp() {
        val width = LocalConfiguration.current.screenWidthDp

        mainUiState = mainViewModel.uiState.collectAsState().value
        scheduleUiState = scheduleViewModel.uiState.collectAsState().value
        gradeUiState = gradeViewModel.uiState.collectAsState().value

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        screens = mapOf(
            Route.SCHEDULE to Screen(
                route = Route.SCHEDULE.toString(),
                resourceId = R.string.schedule_label,
                unselectedIcon = Icons.Outlined.ViewTimeline,
                selectedIcon = Icons.Filled.ViewTimeline,
                enabled = mainUiState.isLogin,
                title = {
                    if (scrollBehavior.state.heightOffset < 0f) {
                        Text(
                            text = stringResource(screens[Route.SCHEDULE]!!.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (scheduleUiState.browsedSemester != null && scheduleUiState.browsedWeek != null) {
                        Text(
                            text = "${
                                AcademicYearAndSemester.getReadableString(
                                    scheduleUiState.semesters.first(),
                                    scheduleUiState.browsedSemester.toString()
                                )
                            } ${
                                stringResource(
                                    R.string.week_indicator,
                                    scheduleUiState.browsedWeek.toString()
                                )
                            }",
                        )
                    }
                },
                actions = { ActionsForSchedule(scheduleViewModel, scheduleUiState) },
                content = { ScheduleScreen(scheduleViewModel) }
            ),
            Route.GRADE to Screen(
                route = Route.GRADE.toString(),
                resourceId = R.string.grade_label,
                unselectedIcon = Icons.Outlined.DriveFileRenameOutline,
                selectedIcon = Icons.Filled.DriveFileRenameOutline,
                enabled = mainUiState.isLogin,
                title = {
                    if (!gradeUiState.isSearchBarShow) {
                        Text(
                            text = stringResource(screens[Route.GRADE]!!.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AnimatedVisibility(
                        visible = gradeUiState.isSearchBarShow,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        NoBorderTextField(
                            value = gradeUiState.searchKeyword,
                            placeholderText = stringResource(R.string.course_name),
                            onValueChange = { gradeViewModel.setSearchKeyword(it) },
                            focusRequester = focusSearchBarRequester
                        )
                    }
                },
                actions = { ActionsForGrade(gradeViewModel, gradeUiState) },
                fab = { FloatingActionButtonForGrade(gradeUiState, gradeViewModel) },
                content = { GradeScreen(gradeUiState, gradeViewModel) }
            ),
            Route.SETTINGS to Screen(
                route = Route.SETTINGS.toString(),
                resourceId = R.string.settings_label,
                unselectedIcon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
                enabled = true,
                content = { SettingsScreen(settingsViewModel, loginViewModel) }
            )
        )

        navHostController = rememberNavController()
        val navBackStackEntry by navHostController.currentBackStackEntryAsState()
        currentDestination = navBackStackEntry?.destination

        BackHandler(gradeUiState)

        CampusHelperTheme(themeViewModel) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (width > 1240) {
                    PermanentNavigationDrawer(
                        drawerContent = {
                            ModalDrawerSheet(drawerTonalElevation = 0.dp) {
                                Text(
                                    stringResource(R.string.app_name),
                                    Modifier
                                        .padding(start = 30.dp)
                                        .padding(vertical = 20.dp)
                                )
                                LargeScreenNavigationItems()
                            }
                        },
                    ) {
                        MobileScaffold(scrollBehavior, true)
                    }
                } else {
                    MobileScaffold(scrollBehavior, false)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MobileScaffold(
        scrollBehavior: TopAppBarScrollBehavior,
        largeScreenMode: Boolean = false,
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                DefaultTopAppBar(scrollBehavior, largeScreenMode)
            },
            bottomBar = {
                if (!largeScreenMode) {
                    DefaultNavigationItems()
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
            floatingActionButton = {
                currentDestination?.route?.let {
                    screens[Route.valueOf(
                        it
                    )]
                }?.let { if (it.enabled) it else null }?.fab?.let { it() }
            }
        ) {
            NavHost(
                navController = navHostController,
                startDestination = screens.values.first { it.enabled }.route,
                modifier = Modifier.padding(it),
            ) {
                screens.values.forEach { screen ->
                    composable(
                        route = screen.route,
                    ) { screen.content() }
                }
            }
        }
    }

    @Composable
    fun DefaultNavigationItems() {
        NavigationBar {
            screens.values.filter { it.enabled }.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = when (currentDestination?.route == screen.route) {
                                true -> screen.selectedIcon
                                false -> screen.unselectedIcon
                            },
                            contentDescription = stringResource(screen.resourceId),
                        )
                    },
                    label = { Text(stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navHostController.navigate(screen.route) {
                            popUpTo(0) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    alwaysShowLabel = false,
                )
            }
        }
    }

    @Composable
    fun LargeScreenNavigationItems() {
        screens.values.filter { it.enabled }.forEach { screen ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = when (currentDestination?.route == screen.route) {
                            true -> screen.selectedIcon
                            false -> screen.unselectedIcon
                        },
                        contentDescription = stringResource(screen.resourceId),
                    )
                },
                label = { Text(stringResource(screen.resourceId), fontWeight = FontWeight.Normal) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navHostController.navigate(screen.route) {
                        popUpTo(0) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DefaultTopAppBar(scrollBehavior: TopAppBarScrollBehavior, largeScreenMode: Boolean) {
        val density = LocalDensity.current

        MediumTopAppBar(
            colors = if (largeScreenMode) TopAppBarDefaults.mediumTopAppBarColors(
                scrolledContainerColor = MaterialTheme.colorScheme.surface,
            ) else TopAppBarDefaults.mediumTopAppBarColors(),
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    currentDestination?.route?.let {
                        screens[Route.valueOf(
                            it
                        )]
                    }?.let { if (it.enabled) it else null }?.title?.let { it() }
                }
            },
            actions = {
                currentDestination?.route?.let {
                    screens[Route.valueOf(
                        it
                    )]
                }?.let { if (it.enabled) it else null }?.actions?.let { Row { it() } }
            },
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                AnimatedVisibility(
                    visible = gradeUiState.isSearchBarShow,
                    enter = slideInHorizontally {
                        with(density) { -80.dp.roundToPx() }
                    },
                    exit = slideOutHorizontally {
                        with(density) { -80.dp.roundToPx() }
                    },
                ) {
                    IconButton(
                        onClick = {
                            gradeViewModel.setIsSearchBarShow(false)
                            gradeViewModel.setSearchKeyword("")
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun BackHandler(
        gradeUiState: GradeUiState,
    ) {
        val scope = rememberCoroutineScope()
        var showExitHint by remember { mutableStateOf(false) }
        BackHandler(true) label@{
            if (gradeUiState.isSearchBarShow) {
                gradeViewModel.setIsSearchBarShow(false)
                gradeViewModel.setSearchKeyword("")
                return@label
            }
            if (showExitHint) {
                scope.launch {
                    ActivityCompat.finishAffinity(this@MainActivity)
                }
                return@label
            }
            showExitHint = true
            scope.launch {
                showExitHint =
                    snackBarHostState.showSnackbar(mainActivity.getString(R.string.message_befor_exit)) != SnackbarResult.Dismissed
            }
        }
    }
}
