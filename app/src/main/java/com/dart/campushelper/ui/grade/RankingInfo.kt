package com.dart.campushelper.ui.grade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState

@Composable
fun RankingInfo(uiState: GradeUiState) {
    return HostRankingType.values()
        .forEach { hostRankingType ->
            ListItem(
                leadingContent = {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(
                                color = when (hostRankingType) {
                                    HostRankingType.GPA -> MaterialTheme.colorScheme.primary
                                    HostRankingType.SCORE -> MaterialTheme.colorScheme.secondary
                                },
                            )
                    )
                },
                supportingContent = {
                    Text(hostRankingType.run {
                        SubRankingType.values()
                            .joinToString("，") {
                                uiState.rankingInfo.getRanking(
                                    this,
                                    it
                                ).run {
                                    "$it ${this?.ranking ?: "-"}/${this?.total ?: "-"}"
                                }
                            }
                            .replaceWithStars(uiState.isScreenshotMode)
                    })
                },
                headlineContent = { Text(hostRankingType.toString()) },
            )
        }
}