package com.dart.campushelper.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
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
fun <T> HorizontalPagerTabRow(
    tabNames: List<String>?,
    dataSource: List<T>?,
    pairRule: (T) -> String,
    itemLayout: @Composable (item: T) -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (!tabNames.isNullOrEmpty()) {
        val pagerState = rememberPagerState { tabNames.size }
        Column {
            SecondaryScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                tabNames.forEachIndexed { index, buildingName ->
                    Tab(
                        text = { Text(buildingName) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.scrollToPage(index) }
                        },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->
                if (dataSource != null) {
                    LazyColumn {
                        items(dataSource.filter { pairRule(it) == tabNames.get(pageIndex) }) {
                            itemLayout(it)
                        }
                    }
                }
            }
        }
    }
}