package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dart.campushelper.repo.Result
import com.dart.campushelper.repo.Status
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LoadingIndicatorStyle {
    CIRCULAR,
    SHIMMER,
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> LoadOnlineDataLayout(
    dataSource: Result<T>,
    loadingIndicatorStyle: LoadingIndicatorStyle = LoadingIndicatorStyle.CIRCULAR,
    loadData: suspend () -> Unit,
    autoLoadingArgs: Array<Any?> = arrayOf(Unit),
    autoLoadWhenDataLoaded: Boolean = false,
    contentWhenDataSourceIsEmpty: @Composable (() -> Unit)? = null,
    contentWhenDataSourceIsNotEmpty: @Composable ((items: T) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    var changeTimes by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    fun onRefresh() = scope.launch {
        isRefreshing = true
        withContext(Dispatchers.IO) {
            loadData()
        }
        isRefreshing = false
    }

    LaunchedEffect(*autoLoadingArgs) {
        if (autoLoadWhenDataLoaded || (changeTimes == 0 && dataSource.data == null) || changeTimes > 0) {
            onRefresh()
        }
        changeTimes++
    }

    val refreshState = rememberPullRefreshState(isRefreshing, ::onRefresh)
    Box(
        modifier = if (loadingIndicatorStyle == LoadingIndicatorStyle.CIRCULAR)
            Modifier.pullRefresh(refreshState)
        else Modifier
            .wrapContentSize()
            .placeholder(
                visible = dataSource.status == Status.LOADING || isRefreshing,
                highlight = PlaceholderHighlight.shimmer()
            )
    ) {
        if (dataSource.status == Status.LOADING || isRefreshing) {
            // Waiting for data to load from network
            if (loadingIndicatorStyle == LoadingIndicatorStyle.CIRCULAR) {
                Box(
                    Modifier
                        .fillMaxSize()
                )
            } else {
                Box(
                    Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer())
                )
            }
        } else if (dataSource.data == null) {
            // If fail to load data from network, show a placeholder
            FailToLoadPlaceholder { onRefresh() }
        } else if (dataSource.data is List<*> && dataSource.data.isEmpty()) {
            contentWhenDataSourceIsEmpty?.let { it() }
        } else {
            contentWhenDataSourceIsNotEmpty?.let { it(dataSource.data) }
        }
        if (loadingIndicatorStyle == LoadingIndicatorStyle.CIRCULAR) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(bottom = DEFAULT_PADDING),
                scale = true
            )
        }
    }
}
