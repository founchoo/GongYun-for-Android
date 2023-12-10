package com.dart.campushelper.ui.component.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants

@Composable
fun SliderPreference(
    value: Float,
    minValue: Float = 1f,
    maxValue: Float,
    steps: Int = (maxValue - minValue + 1).toInt(),
    imageVector: ImageVector? = null,
    title: String,
    description: String,
    hapticFeedbackEnabled: Boolean = false,
    onValueChanged: (value: Float) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val stepLength = (maxValue - minValue + 1) / steps
    Column(
        modifier = Modifier
            .padding(vertical = Constants.DEFAULT_PADDING)
            .padding(start = 25.dp)
            .fillMaxWidth()
    ) {
        if (imageVector != null) {
            Box(
                modifier = Modifier
                    .padding(start = 25.dp)
            ) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        }
        Text(text = title, fontSize = 16.sp)
        Text(
            text = description,
            color = MaterialTheme.colorScheme.outline,
            fontSize = 14.sp
        )
        Slider(
            value = value,
            onValueChange = {
                if (hapticFeedbackEnabled) {
                    if ((it - minValue) % stepLength < 1e-10) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                onValueChanged(it)
            },
            steps = steps,
            valueRange = minValue..maxValue,
        )
    }
}