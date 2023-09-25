package com.dart.campushelper.data

import android.util.Log
import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.model.Cache
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.network.Resource
import com.dart.campushelper.utils.network.ResponseHandler
import org.jsoup.Jsoup
import retrofit2.Response
import java.time.LocalDate
import javax.inject.Inject

class ChaoxingRepository @Inject constructor(
    private val chaoxingService: ChaoxingService,
) {

    private val responseHandler = ResponseHandler()

    suspend fun getGrades(): Resource<List<Grade>> {
        return try {
            val grades = chaoxingService.getGrades().body()!!.results
            Log.d("ChaoxingRepository", "getGrades: $grades")
            responseHandler.handleSuccess(grades)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getStudentRankingInfo(semester: String): Resource<RankingInfo> {
        return try {
            val response = chaoxingService.getStudentRankingInfo(
                enterUniversityYear = MainActivity.userCache.enterUniversityYear,
                semester = semester
            )
            val rankingInfo = RankingInfo()
            Jsoup.parse(response.body()!!).run {
                select("table")[1].select("td").forEachIndexed { index, element ->
                    val value = element.text().contains("/").run {
                        if (this) {
                            element.text().split("/").let {
                                if (it[0] == "" || it[1] == "") {
                                    Pair(0, 0)
                                } else {
                                    Pair(it[0].toInt(), it[1].toInt())
                                }
                            }
                        } else {
                            Pair(0, 0)
                        }
                    }

                    when (index) {
                        1 -> rankingInfo.byGPAByInstitute = value
                        2 -> rankingInfo.byGPAByMajor = value
                        3 -> rankingInfo.byGPAByClass = value
                        5 -> rankingInfo.byScoreByInstitute = value
                        6 -> rankingInfo.byScoreByMajor = value
                        7 -> rankingInfo.byScoreByClass = value
                    }
                }
            }
            responseHandler.handleSuccess(rankingInfo)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getSchedule(): Resource<List<Course>> {
        return try {
            Log.d("ChaoxingRepository", "getSchedule: ${MainActivity.userCache}")
            val courses = chaoxingService.getSchedule(
                semesterYearAndNo = MainActivity.userCache.semesterYearAndNo,
                studentId = MainActivity.userCache.studentId,
                semesterNo = MainActivity.userCache.semesterNo
            ).body()!!
            responseHandler.handleSuccess(courses)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getStudentInfo(): Resource<Cache> {
        return try {
            val info = chaoxingService.getStudentInfo().body()!!.data!!.records[0]
            responseHandler.handleSuccess(Cache(
                semesterYearAndNo = info.dataXnxq!!,
                studentId = info.xh!!,
                enterUniversityYear = info.rxnj!!,
            ))
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getSemesterStartLocalDate(): Resource<LocalDate> {
        return try {
            val response = chaoxingService.getWeekInfo()
            val startWeekMonthAndDay = response.body()!!.data.first().date!!
            var month = startWeekMonthAndDay.split("-")[0]
            if (month.length == 1) {
                month = "0$month"
            }
            var day = startWeekMonthAndDay.split("-")[1]
            if (day.length == 1) {
                day = "0$day"
            }
            responseHandler.handleSuccess(LocalDate.parse("${LocalDate.now().year}-$month-$day"))
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun login(
        username: String,
        password: String
    ): Resource<Response<Unit>> {
        return try {
            val response = chaoxingService.login(
                username = username,
                password = password
            )
            if (response.code() == 302) {
                responseHandler.handleSuccess(response)
            } else {
                Log.d("ChaoxingRepository", "login: 登录失败")
                Resource.error(LOGIN_INFO_ERROR, null)
            }
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }
}