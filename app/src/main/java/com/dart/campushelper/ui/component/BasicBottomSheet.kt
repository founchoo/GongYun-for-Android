package com.dart.campushelper.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicBottomSheet(
    isBottomSheetShow: Boolean,
    @StringRes title: Int,
    isContentLoading: Boolean = false,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    content: @Composable () -> Unit,
) {
    if (isBottomSheetShow) {
        ModalBottomSheet(
            windowInsets = WindowInsets.navigationBars,
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = DEFAULT_PADDING)
                    .fillMaxWidth()
            ) {
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = DEFAULT_PADDING)
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacer)))
                if (isContentLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Box(
                        Modifier.padding(horizontal = DEFAULT_PADDING)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}