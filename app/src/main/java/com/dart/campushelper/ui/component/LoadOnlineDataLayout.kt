package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.dart.campushelper.data.Result
import com.dart.campushelper.data.Status
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

/**
 * A layout that can load data from network and display different content according to the
 * loading state.
 * @param dataSource The data source to load data from network.
 * @param pullRefreshEnabled Whether to enable pull refresh.
 * @param loadData The function to load data from network.
 * @param autoLoadingArgs The arguments to trigger auto loading.
 * @param autoLoadWhenDataLoaded Whether to auto load data when data source is not empty.
 * @param contentWhenDataSourceIsEmpty The content to display when data source is empty.
 * @param contentWhenDataSourceIsNotEmpty The content to display when data source is not empty.
 */
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
            Box(Modifier.fillMaxSize())
        } else if (dataSource.data == null) {
            // If fail to load data from network, show a placeholder
            FailToLoadPlaceholder(::onRefresh)
        } else if (dataSource.data is List<*> && dataSource.data.isEmpty()) {
            contentWhenDataSourceIsEmpty?.let { it() }
        } else {
            contentWhenDataSourceIsNotEmpty?.let { it(dataSource.data) }
        }
        if (loadingIndicatorStyle == LoadingIndicatorStyle.CIRCULAR) {
            PullRefreshIndicator(
                isRefreshing,
                refreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
