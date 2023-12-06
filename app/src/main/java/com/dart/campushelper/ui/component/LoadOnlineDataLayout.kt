package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dart.campushelper.data.Result

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : List<Any>> LoadOnlineDataLayout(
    dataSource: Result<T>?,
    pullRefreshEnabled: Boolean = true,
    loadData: (() -> Unit)? = null,
    contentWhenDataSourceIsEmpty: @Composable (() -> Unit)? = null,
    contentWhenDataSourceIsNotEmpty: @Composable ((items: T) -> Unit)? = null,
) {
    val refreshState = rememberPullRefreshState(dataSource == null, { loadData?.let { it() } })
    Box(modifier = if (pullRefreshEnabled) Modifier.pullRefresh(refreshState) else Modifier) {
        if (dataSource == null) {
            // Waiting for data to load from network
            Box(Modifier.fillMaxSize())
        } else if (dataSource.data == null) {
            // Fail to load data from network
            FailToLoadPlaceholder(loadData)
        } else if (dataSource.data.isEmpty()) {
            contentWhenDataSourceIsEmpty?.let { it() }
        } else {
            contentWhenDataSourceIsNotEmpty?.let { it(dataSource.data) }
        }
        if (pullRefreshEnabled) {
            PullRefreshIndicator(
                dataSource == null,
                refreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
