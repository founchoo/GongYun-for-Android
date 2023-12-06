package com.dart.campushelper.ui

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.receiver.AppWidgetPinnedReceiver
import com.dart.campushelper.receiver.DateChangeReceiver
import com.dart.campushelper.ui.grade.ActionsForGrade
import com.dart.campushelper.ui.grade.FloatingActionButtonForGrade
import com.dart.campushelper.ui.grade.GradeScreen
import com.dart.campushelper.ui.schedule.ActionsForSchedule
import com.dart.campushelper.ui.schedule.ScheduleScreen
import com.dart.campushelper.ui.settings.SettingsScreen
import com.dart.campushelper.ui.theme.CampusHelperTheme
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import com.dart.campushelper.viewmodel.LoginViewModel
import com.dart.campushelper.viewmodel.MainViewModel
import com.dart.campushelper.viewmodel.ScheduleViewModel
import com.dart.campushelper.viewmodel.SettingsViewModel
import com.dart.campushelper.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

lateinit var focusSearchBarRequester: FocusRequester

data class Screen(val route: String, @StringRes val resourceId: Int)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    private val loginViewModel: LoginViewModel by viewModels()

    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val gradeViewModel: GradeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val mainViewModel: MainViewModel by viewModels()

    private val mDateReceiver = DateChangeReceiver()

    companion object {
        lateinit var snackBarHostState: SnackbarHostState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()
            snackBarHostState = remember { SnackbarHostState() }
            focusSearchBarRequester = remember { FocusRequester() }
            CampusHelperApp(mainViewModel, scope)
        }
    }

    @SuppressLint("CoroutineCreationDuringComposition", "StateFlowValueCalledInComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CampusHelperApp(mainViewModel: MainViewModel, scope: CoroutineScope) {

        val density = LocalDensity.current

        val interactionSource = remember { MutableInteractionSource() }

        val mainUiState by mainViewModel.uiState.collectAsState()
        val scheduleUiState by scheduleViewModel.uiState.collectAsState()
        val gradeUiState by gradeViewModel.uiState.collectAsState()

        val schedule = Screen("schedule", R.string.schedule_label)
        val program = Screen("program", R.string.program_label)
        val grade = Screen("grade", R.string.grade_label)
        val settings = Screen("settings", R.string.settings_label)

        val idsOutline = mapOf(
            schedule to Icons.Outlined.ViewTimeline,
            grade to Icons.Outlined.DriveFileRenameOutline,
            settings to Icons.Outlined.Settings
        )
        val idsFill = mapOf(
            schedule to Icons.Filled.ViewTimeline,
            grade to Icons.Filled.DriveFileRenameOutline,
            settings to Icons.Filled.Settings,
        )

        BackHandler(gradeUiState, gradeViewModel, scope)

        CampusHelperTheme(themeViewModel) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    when (currentDestination?.route) {
                                        schedule.route -> {
                                            Text(
                                                text = stringResource(schedule.resourceId),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            TooltipBox(
                                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                                tooltip = {
                                                    RichTooltip(
                                                        title = {
                                                            Text(
                                                                stringResource(R.string.switch_schedule),
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        },
                                                        action = {
                                                            TextButton(
                                                                onClick = {
                                                                    scope.launch {
                                                                        scheduleUiState.holdingSemesterTooltipState.dismiss()
                                                                    }
                                                                }
                                                            ) {
                                                                Text(stringResource(R.string.close))
                                                            }
                                                        }) {
                                                        Text(stringResource(R.string.switch_schedule_hint))
                                                    }
                                                },
                                                state = scheduleUiState.holdingSemesterTooltipState
                                            ) {
                                                Column(
                                                    Modifier
                                                        .clip(RoundedCornerShape(5.dp))
                                                        .clickable {
                                                            scheduleViewModel.setIsShowWeekSliderDialog(
                                                                true
                                                            )
                                                        },
                                                ) {
                                                    Text(
                                                        text = scheduleUiState.browsedSemester,
                                                        style = MaterialTheme.typography.labelSmall,
                                                    )
                                                    Text(
                                                        text = stringResource(
                                                            R.string.week_indicator,
                                                            scheduleUiState.browsedWeek.toString()
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        style = MaterialTheme.typography.labelSmall,
                                                    )
                                                }
                                            }
                                        }

                                        grade.route -> {
                                            if (!gradeUiState.isSearchBarShow) {
                                                Text(
                                                    text = stringResource(grade.resourceId),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            AnimatedVisibility(
                                                visible = gradeUiState.isSearchBarShow,
                                                enter = fadeIn(),
                                                exit = fadeOut(),
                                            ) {
                                                BasicTextField(
                                                    value = gradeUiState.searchKeyword,
                                                    onValueChange = {
                                                        gradeViewModel.setSearchKeyword(it)
                                                    },
                                                    interactionSource = interactionSource,
                                                    textStyle = TextStyle(
                                                        fontSize = 16.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    modifier = Modifier
                                                        .focusRequester(focusSearchBarRequester),
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                ) {
                                                    OutlinedTextFieldDefaults.DecorationBox(
                                                        value = gradeUiState.searchKeyword,
                                                        innerTextField = it,
                                                        enabled = true,
                                                        singleLine = true,
                                                        interactionSource = interactionSource,
                                                        visualTransformation = VisualTransformation.None,
                                                        placeholder = {
                                                            Text(
                                                                text = stringResource(R.string.course_name),
                                                            )
                                                        },
                                                        container = {
                                                            OutlinedTextFieldDefaults.ContainerBox(
                                                                enabled = true,
                                                                isError = false,
                                                                interactionSource = interactionSource,
                                                                colors = OutlinedTextFieldDefaults.colors(
                                                                    focusedBorderColor = Color.Transparent,
                                                                    unfocusedBorderColor = Color.Transparent,
                                                                ),
                                                                focusedBorderThickness = 0.dp,
                                                                unfocusedBorderThickness = 0.dp,
                                                            )
                                                        }
                                                    )
                                                }
                                                LaunchedEffect(Unit) {
                                                    focusSearchBarRequester.requestFocus()
                                                }
                                            }
                                        }

                                        settings.route -> {
                                            Text(
                                                text = stringResource(settings.resourceId),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                if (currentDestination?.route == schedule.route) {
                                    ActionsForSchedule(
                                        scheduleViewModel,
                                        scheduleUiState
                                    )
                                } else if (currentDestination?.route == grade.route) {
                                    ActionsForGrade(
                                        viewModel = gradeViewModel,
                                        uiState = gradeUiState
                                    )
                                }
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
                    },
                    bottomBar = {
                        NavigationBar {
                            (if (mainUiState.isLogin)
                                listOf(
                                    schedule,
                                    grade,
                                    settings
                                ) else
                                listOf(settings)).forEach { screen ->
                                NavigationBarItem(
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    icon = {
                                        TooltipBox(
                                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                            tooltip = {
                                                PlainTooltip {
                                                    Text(stringResource(screen.resourceId))
                                                }
                                            },
                                            state = rememberTooltipState()
                                        ) {
                                            Icon(
                                                imageVector = when (currentDestination?.route == screen.route) {
                                                    true -> idsFill[screen]!!
                                                    false -> idsOutline[screen]!!
                                                },
                                                contentDescription = stringResource(screen.resourceId)
                                            )
                                        }
                                    },
                                    label = { Text(stringResource(screen.resourceId)) },
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
                                grade.route -> FloatingActionButtonForGrade(
                                    gradeUiState,
                                    gradeViewModel
                                )

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
    fun BackHandler(
        gradeUiState: GradeUiState,
        gradeViewModel: GradeViewModel,
        scope: CoroutineScope
    ) {
        var showExitHint by remember { mutableStateOf(false) }
        BackHandler(true) {
            if (gradeUiState.isSearchBarShow) {
                gradeViewModel.setIsSearchBarShow(false)
                gradeViewModel.setSearchKeyword("")
                return@BackHandler
            }
            if (showExitHint) {
                scope.launch {
                    ActivityCompat.finishAffinity(this@MainActivity)
                }
                return@BackHandler
            }
            showExitHint = true
            scope.launch {
                showExitHint =
                    snackBarHostState.showSnackbar(context.getString(R.string.message_befor_exit)) != SnackbarResult.Dismissed
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