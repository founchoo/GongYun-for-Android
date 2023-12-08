package com.dart.campushelper.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import kotlinx.coroutines.runBlocking

@Composable
fun FailToLoadPlaceholder(
    retry: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(5.dp)),
    ) {
        Spacer(Modifier.width(15.dp))
        Text(
            text = stringResource(R.string.network_connection_error),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = {
                runBlocking { retry() }
            },
            colors =  ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        ) {
            Text(
                text = stringResource(R.string.retry),
            )
        }
        Spacer(Modifier.width(15.dp))
    }
}