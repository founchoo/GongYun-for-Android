package com.dart.campushelper.api

import android.util.Log
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.WeekInfoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.Instant
import java.time.format.DateTimeFormatter


interface ChaoxingService {

    @GET("xsd/xsdzgcjcx/xsdQueryXszgcjList")
    suspend fun getGrades(
        @Query("gridtype") gridType: String = "jqgrid",
        @Query("queryFields") queryFields: String = "id,xnxq,kcmc,xf,kcxz,ksxs,xdxz,zhcj,hdxf,",
        @Query("_search") isSearch: String = "false",
        @Query("nd") nd: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
        @Query("page.size") pageSize: String = "500",
        @Query("page.pn") pagePn: String = "1",
        @Query("sort") sort: String = "id",
        @Query("order") order: String = "asc",
        @Query("startXnxq") startSemester: String = "001",
        @Query("endXnxq") endSemester: String = "001",
        @Query("sfjg") sfjq: String = "",
        @Query("query.startXnxq||") queryStartSemester: String = "001",
        @Query("query.endXnxq||") queryEndSemester: String = "001",
        @Query("query.sfjg||") querySfjq: String = "",
    ): Response<GradeResponse>

    @GET("cjgl/xscjbbdy/getXscjpm")
    suspend fun getStudentRankingInfo(
        @Query("sznj") enterUniversityYear: String,
        @Query("xnxq") semester: String
    ): Response<String>

    @GET("pkgl/xskb/sdpkkbList")
    suspend fun getSchedule(
        @Query("xnxq") semesterYearAndNo: String,
        @Query("xhid") studentId: String,
        @Query("xqdm") semesterNo: String,
    ): Response<List<Course>>

    @POST("cjgl/xscjbbdy/printdgxscj")
    suspend fun getStudentInfo(): Response<StudentInfoResponse>

    @FormUrlEncoded
    @POST("getXqByZc")
    suspend fun getWeekInfo(
        @Field("zc") weekNo: Int = 0
    ): Response<WeekInfoResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("jcaptchaCode") jcaptchaCode: String = "",
        @Field("rememberMe") rememberMe: Int = 1,
    ): Response<Unit>

    companion object {
        private const val BASE_URL = "https://hbut.jw.chaoxing.com/admin/"

        var cookies = MutableStateFlow<List<Cookie>>(emptyList())

        fun create(): ChaoxingService {

            val client = OkHttpClient.Builder()
                .followRedirects(false)
                .addInterceptor {
                    var error: Boolean
                    val request = it.request()
                    Log.d("okhttp.OkHttpClient", "request: $request")
                    var response = okhttp3.Response.Builder()
                        .request(it.request())
                        .protocol(okhttp3.Protocol.HTTP_1_1)
                        .message("Not found response body")
                        .code(404)
                        .body("Not found response body".toResponseBody(null))
                        .build()
                    error = false
                    try {
                        response = it.proceed(request)
                        Log.d("okhttp.OkHttpClient", "response: $response")
                    } catch (e: Exception) {
                        Log.d("okhttp.OkHttpClient", "error: $e")
                    }
                    Log.d("okhttp.OkHttpClient", "response: $response")
                    Log.d("okhttp.OkHttpClient", "error: $error")
                    response
                }
                .cookieJar(object : CookieJar {

                    override fun loadForRequest(url: HttpUrl): List<Cookie> {
                        Log.d(
                            "okhttp.OkHttpClient",
                            "loadForRequest: cookies.size: ${cookies.value.size}"
                        )
                        return cookies.value
                    }

                    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                        val count = cookies.count { it.name == "rememberMe" }
                        if (url.toString() == "${BASE_URL}login" && count == 2) {
                            this@Companion.cookies.value = cookies
                        }
                    }
                })
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChaoxingService::class.java)
        }
    }
}
