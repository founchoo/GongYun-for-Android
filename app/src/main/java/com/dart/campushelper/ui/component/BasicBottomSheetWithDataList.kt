package com.dart.campushelper.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun <T> BasicBottomSheetWithDataList(
    isBottomSheetShow: Boolean,
    @StringRes title: Int,
    @StringRes descriptionWhenItemSourceIsEmpty: Int,
    isContentLoading: Boolean,
    itemSource: List<T>?,
    onDismissRequest: () -> Unit,
    contentWhenItemSourceIsNotEmpty: @Composable (List<T>) -> Unit,
) {
    BasicBottomSheetWithDataList(
        isBottomSheetShow = isBottomSheetShow,
        title = title,
        descriptionWhenItemSourceIsEmpty = stringResource(descriptionWhenItemSourceIsEmpty),
        isContentLoading = isContentLoading,
        itemSource = itemSource,
        onDismissRequest = onDismissRequest,
        contentWhenItemSourceIsNotEmpty = contentWhenItemSourceIsNotEmpty,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> BasicBottomSheetWithDataList(
    isBottomSheetShow: Boolean,
    @StringRes title: Int,
    descriptionWhenItemSourceIsEmpty: String,
    descriptionWhenItemSourceIsNotEmpty: String? = null,
    isContentLoading: Boolean,
    itemSource: List<T>?,
    onDismissRequest: () -> Unit,
    contentWhenItemSourceIsNotEmpty: @Composable (List<T>) -> Unit,
) {
    BasicBottomSheet(
        isBottomSheetShow = isBottomSheetShow,
        title = title,
        onDismissRequest = onDismissRequest,
        isContentLoading = isContentLoading,
    ) {
        Column {
            if (itemSource.isNullOrEmpty()) {
                Text(
                    text = descriptionWhenItemSourceIsEmpty,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                if (descriptionWhenItemSourceIsNotEmpty != null) {
                    Text(
                        text = descriptionWhenItemSourceIsNotEmpty,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                contentWhenItemSourceIsNotEmpty(itemSource)
            }
        }
    }
}