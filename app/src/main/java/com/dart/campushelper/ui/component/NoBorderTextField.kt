package com.dart.campushelper.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoBorderTextField(
    text: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = text,
        onValueChange = { onValueChange(it) },
        interactionSource = interactionSource,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = (if (focusRequester == null) Modifier else Modifier
            .focusRequester(focusRequester)).fillMaxWidth(),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        maxLines = 1,
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = text,
            innerTextField = it,
            enabled = true,
            singleLine = true,
            interactionSource = interactionSource,
            visualTransformation = VisualTransformation.None,
            placeholder = { Text(placeholderText) },
            container = {
                OutlinedTextFieldDefaults.ContainerBox(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                    focusedBorderThickness = 0.dp,
                    unfocusedBorderThickness = 0.dp,
                )
            }
        )
    }
    LaunchedEffect(Unit) {
        focusRequester?.requestFocus()
    }
}