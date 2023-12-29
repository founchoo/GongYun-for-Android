package com.dart.campushelper.ui.component.listitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING

@Composable
fun SliderListItem(
    value: Float,
    minValue: Float = 1f,
    maxValue: Float,
    steps: Int = (maxValue - minValue + 1).toInt(),
    leadingImageVector: ImageVector? = null,
    headlineText: String,
    supportingText: String,
    hapticFeedbackEnabled: Boolean = false,
    onValueChanged: (value: Float) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val stepLength = (maxValue - minValue + 1) / steps
    Column {
        BasicListItem(
            headlineText = headlineText,
            supportingText = supportingText,
            leadingImageVector = leadingImageVector,
            trailingContent = {
                Text(
                    text = value.toInt().toString(),
                )
            }
        )
        Slider(
            modifier = Modifier.padding(horizontal = DEFAULT_PADDING),
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