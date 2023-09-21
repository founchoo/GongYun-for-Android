package com.dart.campushelper.model

data class RankingInfo(
    var byGPAByInstitute: Pair<Int, Int> = Pair(0, 0),
    var byGPAByMajor: Pair<Int, Int> = Pair(0, 0),
    var byGPAByClass: Pair<Int, Int> = Pair(0, 0),

    var byScoreByInstitute: Pair<Int, Int> = Pair(0, 0),
    var byScoreByMajor: Pair<Int, Int> = Pair(0, 0),
    var byScoreByClass: Pair<Int, Int> = Pair(0, 0),
)