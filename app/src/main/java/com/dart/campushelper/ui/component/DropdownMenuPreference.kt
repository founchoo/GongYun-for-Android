package com.dart.campushelper.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants

@Composable
fun <T> DropdownMenuPreference(
    value: T,
    imageVector: ImageVector? = null,
    title: String,
    selections: List<T>,
    onValueChanged: (index: Int, value: T) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .clickable {
                expanded.value = true
            }
            .padding(vertical = Constants.DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        if (imageVector != null) {
            Box(
                modifier = Modifier
                    .padding(start = 25.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 25.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, fontSize = 16.sp)
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp
            )
        }
        Box(modifier = Modifier.weight(1f, true)) {
            DropdownMenu(
                offset = DpOffset((-20).dp, 0.dp),
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                selections.forEachIndexed { index, selection ->
                    DropdownMenuItem(
                        modifier = Modifier.background(
                            if (selection == value) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                        ),
                        text = { Text(selection.toString()) },
                        onClick = {
                            expanded.value = false
                            onValueChanged(index, selection)
                        }
                    )
                }
            }
        }
    }
}
