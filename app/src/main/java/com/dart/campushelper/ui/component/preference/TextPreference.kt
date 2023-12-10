package com.dart.campushelper.ui.component.preference

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.utils.Constants

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
            .padding(horizontal = Constants.DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = Constants.DEFAULT_PADDING)
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
fun TextPreference(
    title: String,
    description: String,
    painter: Painter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(horizontal = Constants.DEFAULT_PADDING)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = Constants.DEFAULT_PADDING)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.align(Alignment.CenterVertically)) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Image(
                        painter = painter, contentDescription = null, modifier = Modifier
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .size(24.dp)
                    )
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
