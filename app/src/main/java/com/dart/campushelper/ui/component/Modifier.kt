package com.dart.campushelper.ui.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

fun Modifier.defaultHorizontalFadingEdge() = this.fadingEdge(
    Brush.horizontalGradient(
        0f to Color.Transparent,
        0.05f to Color.Black,
        0.95f to Color.Black,
        1f to Color.Transparent
    )
)

fun Modifier.defaultVerticalFadingEdge() = this.fadingEdge(
    Brush.verticalGradient(
        0f to Color.Transparent,
        0.05f to Color.Black,
        0.95f to Color.Black,
        1f to Color.Transparent
    )
)
