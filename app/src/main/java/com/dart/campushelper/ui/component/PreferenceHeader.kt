package com.dart.campushelper.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants

@Composable
fun PreferenceHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = Constants.DEFAULT_PADDING,
            top = Constants.SMALL_PADDING,
            bottom = Constants.SMALL_PADDING
        ),
        fontSize = 14.sp
    )
}
