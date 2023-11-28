package com.dart.campushelper.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING
import com.dart.campushelper.utils.Constants.Companion.SMALL_PADDING
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.overlayingComponent
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.chart.dimensions.HorizontalDimensions
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.appendCompat
import com.patrykandpatrick.vico.core.extension.copyColor
import com.patrykandpatrick.vico.core.extension.sumOf
import com.patrykandpatrick.vico.core.extension.transformToSpannable
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter

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

@Composable
fun <T> DropdownMenuPreference(
    value: T,
    imageVector: ImageVector,
    title: String,
    selections: List<T>,
    onValueChanged: (value: T) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .clickable {
                expanded.value = true
            }
            .padding(vertical = DEFAULT_PADDING)
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
                selections.forEach { selection ->
                    DropdownMenuItem(
                        modifier = Modifier.background(
                            if (selection == value) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                        ),
                        text = { Text(selection.toString()) },
                        onClick = {
                            expanded.value = false
                            onValueChanged(selection)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchPreference(
    value: Boolean,
    imageVector: ImageVector,
    title: String,
    description: String? = null,
    onValueChanged: (value: Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .clickable {
                onValueChanged(!value)
            }
            .padding(vertical = DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.align(Alignment.CenterVertically)) {
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
            ) {
                Text(text = title, fontSize = 16.sp)
                if (description != null) {
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
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

@Composable
fun TextPreference(
    title: String,
    description: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(horizontal = DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = DEFAULT_PADDING)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(imageVector = imageVector, contentDescription = null)
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 25.dp)
                ) {
                    Text(text = title, fontSize = 16.sp)
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceHeader(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = DEFAULT_PADDING,
            top = SMALL_PADDING,
            bottom = SMALL_PADDING
        ),
        fontSize = 14.sp
    )
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

val DiagonalLinesShape = object : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(Path().apply {
            val canvasWidth = size.width
            for (i in 0 until canvasWidth.toInt() step 20) {
                moveTo(i.toFloat(), 0f)
                lineTo(0f, i.toFloat())
            }
        })
    }
}

fun Modifier.diagonalLines(color: Color, strokeWidth: Float, step: Int, padding: Float = 3f): Modifier = composed {
    drawWithContent {
        drawContent()
        val canvasWidth = size.width
        val canvasHeight = size.height
        for (i in 0 until canvasHeight.toInt() step step) {
            // Left
            drawLine(
                strokeWidth = strokeWidth,
                color = color,
                start = Offset(0f, i.toFloat()),
                end = Offset(padding, i - padding)
            )
            // Right
            drawLine(
                strokeWidth = strokeWidth,
                color = color,
                start = Offset(canvasWidth - padding, i.toFloat()),
                end = Offset(canvasWidth, i - padding)
            )
        }
        for (i in 0 until canvasWidth.toInt() step step) {
            // Top
            drawLine(
                strokeWidth = strokeWidth,
                color = color,
                start = Offset(i.toFloat(), padding),
                end = Offset(i + padding, 0f)
            )
            // Bottom
            drawLine(
                strokeWidth = strokeWidth,
                color = color,
                start = Offset(i.toFloat(), canvasHeight),
                end = Offset(i + padding, canvasHeight - padding)
            )
        }
    }
}

@Composable
internal fun rememberMarker(formatter: (raw: Float) -> String): Marker {
    val labelBackgroundColor = MaterialTheme.colorScheme.surface
    val labelBackground = remember(labelBackgroundColor) {
        ShapeComponent(labelBackgroundShape, labelBackgroundColor.toArgb()).setShadow(
            radius = LABEL_BACKGROUND_SHADOW_RADIUS,
            dy = LABEL_BACKGROUND_SHADOW_DY,
            applyElevationOverlay = true,
        )
    }
    val label = textComponent(
        background = labelBackground,
        lineCount = LABEL_LINE_COUNT,
        padding = labelPadding,
        typeface = Typeface.MONOSPACE,
    )
    val indicatorInnerComponent =
        shapeComponent(Shapes.pillShape, MaterialTheme.colorScheme.surface)
    val indicatorCenterComponent = shapeComponent(Shapes.pillShape, Color.White)
    val indicatorOuterComponent = shapeComponent(Shapes.pillShape, Color.White)
    val indicator = overlayingComponent(
        outer = indicatorOuterComponent,
        inner = overlayingComponent(
            outer = indicatorCenterComponent,
            inner = indicatorInnerComponent,
            innerPaddingAll = indicatorInnerAndCenterComponentPaddingValue,
        ),
        innerPaddingAll = indicatorCenterAndOuterComponentPaddingValue,
    )
    val guideline = lineComponent(
        MaterialTheme.colorScheme.onSurface.copy(GUIDELINE_ALPHA),
        guidelineThickness,
        guidelineShape,
    )
    return remember(label, indicator, guideline) {
        object : MarkerComponent(label, indicator, guideline) {
            init {
                labelFormatter =
                    MarkerLabelFormatter { markedEntries, _ ->
                        markedEntries.transformToSpannable(
                            prefix = if (markedEntries.size > 1) formatter(markedEntries.sumOf { it.entry.y }) + " (" else "",
                            postfix = if (markedEntries.size > 1) ")" else "",
                            separator = "; ",
                        ) { model ->
                            appendCompat(
                                formatter(model.entry.y),
                                ForegroundColorSpan(model.color),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                            )
                        }
                    }
                indicatorSizeDp = INDICATOR_SIZE_DP
                onApplyEntryColor = { entryColor ->
                    indicatorOuterComponent.color =
                        entryColor.copyColor(INDICATOR_OUTER_COMPONENT_ALPHA)
                    with(indicatorCenterComponent) {
                        color = entryColor
                        setShadow(
                            radius = INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS,
                            color = entryColor
                        )
                    }
                }
            }

            override fun getInsets(
                context: MeasureContext,
                outInsets: Insets,
                horizontalDimensions: HorizontalDimensions,
            ) = with(context) {
                outInsets.top = label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels +
                        LABEL_BACKGROUND_SHADOW_RADIUS.pixels * SHADOW_RADIUS_MULTIPLIER -
                        LABEL_BACKGROUND_SHADOW_DY.pixels
            }
        }
    }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS = 4f
private const val LABEL_BACKGROUND_SHADOW_DY = 2f
private const val LABEL_LINE_COUNT = 1
private const val GUIDELINE_ALPHA = .2f
private const val INDICATOR_SIZE_DP = 36f
private const val INDICATOR_OUTER_COMPONENT_ALPHA = 32
private const val INDICATOR_CENTER_COMPONENT_SHADOW_RADIUS = 12f
private const val GUIDELINE_DASH_LENGTH_DP = 8f
private const val GUIDELINE_GAP_LENGTH_DP = 4f
private const val SHADOW_RADIUS_MULTIPLIER = 1.3f

private val labelBackgroundShape = MarkerCorneredShape(Corner.FullyRounded)
private val labelHorizontalPaddingValue = 8.dp
private val labelVerticalPaddingValue = 4.dp
private val labelPadding = dimensionsOf(labelHorizontalPaddingValue, labelVerticalPaddingValue)
private val indicatorInnerAndCenterComponentPaddingValue = 5.dp
private val indicatorCenterAndOuterComponentPaddingValue = 10.dp
private val guidelineThickness = 2.dp
private val guidelineShape =
    DashedShape(Shapes.pillShape, GUIDELINE_DASH_LENGTH_DP, GUIDELINE_GAP_LENGTH_DP)
