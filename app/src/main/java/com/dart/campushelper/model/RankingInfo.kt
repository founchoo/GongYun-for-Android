package com.dart.campushelper.model

class RankingInfo {
    private var data: MutableMap<Pair<HostRankingType, SubRankingType>, Ranking> = mutableMapOf()

    fun getRanking(hostRankingType: HostRankingType, subRankingType: SubRankingType): Ranking? {
        return data[Pair(hostRankingType, subRankingType)]
    }

    fun setRanking(hostRankingType: HostRankingType, subRankingType: SubRankingType, ranking: Ranking) {
        data[Pair(hostRankingType, subRankingType)] = ranking
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
            GPA -> "平均学分绩点"
            SCORE -> "算术平均分"
        }
    }
}

enum class SubRankingType {
    INSTITUTE, MAJOR, CLASS;

    override fun toString(): String {
        return when (this) {
            INSTITUTE -> "学院"
            MAJOR -> "专业"
            CLASS -> "班级"
        }
    }
}
