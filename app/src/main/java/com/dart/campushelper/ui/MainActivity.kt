package com.dart.campushelper.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.receiver.AppWidgetPinnedReceiver
import com.dart.campushelper.receiver.DateChangeReceiver
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mDateReceiver = DateChangeReceiver()

    companion object {
        lateinit var snackBarHostState: SnackbarHostState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            snackBarHostState = remember { SnackbarHostState() }
            var showExitHint by remember { mutableStateOf(false) }
            BackHandler(true) {
                if (showExitHint) {
                    Root.scope.launch {
                        ActivityCompat.finishAffinity(this@MainActivity)
                    }
                    return@BackHandler
                }
                showExitHint = true
                Root.scope.launch {
                    showExitHint =
                        snackBarHostState.showSnackbar("再按一次以退出") != SnackbarResult.Dismissed
                }
            }
            Root(App.container).Create()
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
