package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING

@Composable
fun ColumnCard(
    useErrorColor: Boolean = false,
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    isElevated: Boolean = true,
    actions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    return ElevatedCard(
        colors = if (useErrorColor) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ) else CardDefaults.elevatedCardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DEFAULT_PADDING),
        elevation = if (isElevated) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (useErrorColor) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(5.dp))
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.W900,
                    color = if (useErrorColor) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                if (actions != null) {
                    actions()
                }
            }
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            content()
        }
    }
}
