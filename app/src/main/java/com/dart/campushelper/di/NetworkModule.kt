package com.dart.campushelper.di

import com.dart.campushelper.App.Companion.instance
import com.dart.campushelper.R
import com.dart.campushelper.api.NetworkService
import com.dart.campushelper.data.DataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideNetworkService(
        dataStoreRepository: DataStoreRepository
    ): NetworkService {

        val client = OkHttpClient.Builder()
            .followRedirects(false)
            .cookieJar(NetworkCookieJar(dataStoreRepository))
            .addInterceptor { chain ->
                try {
                    chain.proceed(chain.request())
                } catch (e: Exception) {
                    okhttp3.Response.Builder()
                        .protocol(Protocol.HTTP_1_1)
                        .code(502)
                        .request(chain.request())
                        .message(instance.getString(R.string.network_connection_error))
                        .body(instance.getString(R.string.network_connection_error).toResponseBody())
                        .build()
                }
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkService.BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(NetworkService::class.java)
    }
}

class NetworkCookieJar @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : CookieJar {

    val scope = CoroutineScope(Dispatchers.IO)

    private var cookies: List<Cookie>

    private val cookiesStateFlow = dataStoreRepository.observeCookies().stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking { dataStoreRepository.observeCookies().first() }
    )

    init {
        cookies = runBlocking { cookiesStateFlow.first() }
        scope.launch {
            // Log.d("okhttp.OkHttpClient", "init: cookies.size: ${cookies.size}")
            dataStoreRepository.observeCookies().collect {
                cookies = it
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        /*Log.d(
            "okhttp.OkHttpClient",
            "loadForRequest: cookies.size: ${cookies.size}"
        )*/
        return cookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val count = cookies.count { it.name == "rememberMe" }
        if (url.toString() == "${NetworkService.BASE_URL}login" && count == 2) {
            // Log.d("okhttp.OkHttpClient", "saveFromResponse: cookies.size: ${cookies.size}")
            this@NetworkCookieJar.cookies = cookies
            scope.launch {
                this@NetworkCookieJar.dataStoreRepository.changeCookies(cookies)
            }
        }
    }
}
