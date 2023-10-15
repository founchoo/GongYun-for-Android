package com.dart.campushelper.api

import com.dart.campushelper.model.CalendarItem
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.StudentInfoResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.Instant
import java.time.format.DateTimeFormatter

interface ChaoxingService {

    @GET("system/zy/xlgl/getData/{semesterYearAndNo}")
    fun getCalendar(
        @Path("semesterYearAndNo") semesterYearAndNo: String,
    ): Call<List<CalendarItem>>

    @GET("xsd/xsdzgcjcx/xsdQueryXszgcjList")
    fun getGrades(
        @Query("gridtype") gridType: String? = "jqgrid",
        @Query("queryFields") queryFields: String? = "id,xnxq,kcmc,xf,kcxz,ksxs,xdxz,zhcj,hdxf,",
        @Query("_search") isSearch: String? = "false",
        @Query("nd") nd: String? = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
        @Query("page.size") pageSize: String? = "500",
        @Query("page.pn") pagePn: String? = "1",
        @Query("sort") sort: String? = "id",
        @Query("order") order: String? = "asc",
        @Query("startXnxq") startSemester: String? = "001",
        @Query("endXnxq") endSemester: String? = "001",
        @Query("sfjg") sfjq: String? = "",
        @Query("query.startXnxq||") queryStartSemester: String? = "001",
        @Query("query.endXnxq||") queryEndSemester: String? = "001",
        @Query("query.sfjg||") querySfjq: String? = "",
    ): Call<GradeResponse>

    @GET("cjgl/xscjbbdy/getXscjpm")
    fun getStudentRankingInfo(
        @Query("sznj") enterUniversityYear: String,
        @Query("xnxq") semester: String
    ): Call<String>

    @GET("pkgl/xskb/sdpkkbList")
    fun getSchedule(
        @Query("xnxq") semesterYearAndNo: String,
        @Query("xhid") studentId: String,
        @Query("xqdm") semesterNo: String,
    ): Call<List<Course>>

    @POST("cjgl/xscjbbdy/printdgxscj")
    fun getStudentInfo(): Call<StudentInfoResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("jcaptchaCode") jcaptchaCode: String = "",
        @Field("rememberMe") rememberMe: Int = 1,
    ): Call<Unit>

    companion object {
        const val BASE_URL = "https://hbut.jw.chaoxing.com/admin/"
    }
}
