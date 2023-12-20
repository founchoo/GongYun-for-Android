package com.dart.campushelper.model

import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.R

class RankingInfo {
    private var data: MutableMap<Pair<HostRankingType, SubRankingType>, Ranking> = mutableMapOf()

    fun getRanking(hostRankingType: HostRankingType, subRankingType: SubRankingType): Ranking? {
        return data[Pair(hostRankingType, subRankingType)]
    }

    fun setRanking(hostRankingType: HostRankingType, subRankingType: SubRankingType, ranking: Ranking) {
        data[Pair(hostRankingType, subRankingType)] = ranking
    }

    companion object {
        fun mock(): RankingInfo {
            return RankingInfo().apply {
                HostRankingType.values().forEach { host ->
                    SubRankingType.values().forEach { sub ->
                        this.setRanking(host, sub, Ranking(23, 800))
                    }
                }
            }
        }
    }
}

data class Ranking (
    var ranking: Int = 0,
    var total: Int = 0,
)

enum class HostRankingType {
    GPA, SCORE;

    override fun toString(): String {
        return when (this) {
            GPA -> context.getString(R.string.gpa_label)
            SCORE -> context.getString(R.string.arithmetic_mean_score)
        }
    }
}

enum class SubRankingType {
    INSTITUTE, MAJOR, CLASS;

    override fun toString(): String {
        return when (this) {
            INSTITUTE -> context.getString(R.string.institute)
            MAJOR -> context.getString(R.string.major)
            CLASS -> context.getString(R.string._class)
        }
    }
}
