package com.dart.campushelper.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T, E> HorizontalPagerTabRow(
    isHeaderVisible: Boolean = true,
    tabs: List<E>?,
    pagerState: PagerState = rememberPagerState { tabs?.size ?: 0 },
    dataSource: List<T>?,
    pageContent: @Composable (List<T>, E) -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (!tabs.isNullOrEmpty()) {
        Column {
            if (isHeaderVisible) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                ) {
                    tabs.forEachIndexed { index, buildingName ->
                        Tab(
                            text = { Text(buildingName.toString()) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.scrollToPage(index) }
                            },
                        )
                    }
                }
            }
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                if (dataSource != null) {
                    pageContent(dataSource, tabs[pageIndex])
                }
            }
        }
    }
}