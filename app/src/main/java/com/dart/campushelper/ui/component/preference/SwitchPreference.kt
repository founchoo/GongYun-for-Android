package com.dart.campushelper.ui.component.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants

@Composable
fun SwitchPreference(
    value: Boolean,
    imageVector: ImageVector,
    title: String,
    description: String? = null,
    onValueChanged: (value: Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .clickable {
                onValueChanged(!value)
            }
            .padding(vertical = Constants.DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(start = 25.dp)
                .align(Alignment.CenterVertically)
        ) {
            Icon(imageVector = imageVector, contentDescription = null)
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 25.dp)
                .weight(1f)
        ) {
            Text(text = title, fontSize = 16.sp)
            if (description != null) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = {
                onValueChanged(it)
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 20.dp)
        )
    }
}
