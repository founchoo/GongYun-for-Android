package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R

@Composable
fun FailToLoadPlaceholder(
    retry: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.network_connection_error))
        Spacer(Modifier.width(5.dp))
        TextButton(onClick = {
            retry?.let { it() }
        }) {
            Text(
                text = stringResource(R.string.retry),
            )
        }
    }
}