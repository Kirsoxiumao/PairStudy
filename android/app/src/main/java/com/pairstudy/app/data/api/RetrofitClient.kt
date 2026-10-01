package com.pairstudy.app.data.api
import com.pairstudy.app.BuildConfig
import com.pairstudy.app.data.local.SessionStore
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
class RetrofitClient(private val tokenProvider: () -> String?) {
    val base = BuildConfig.BASE_URL.toHttpUrl()
    val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val active = ServerConfig.url.toHttpUrl()
            val isApi = original.url.host == base.host && original.url.port == base.port && original.url.scheme == base.scheme && original.url.encodedPath.startsWith("/api/")
            val request = if (isApi) original.newBuilder().url(original.url.newBuilder().scheme(active.scheme).host(active.host).port(active.port).build()).build() else original
            val sameOrigin = request.url.host == active.host && request.url.port == active.port && request.url.scheme == active.scheme
            val token = tokenProvider()
            chain.proceed(if (sameOrigin && token != null) request.newBuilder().header("Authorization", "Bearer $token").build() else request)
        }.build()
    val api: ApiService = Retrofit.Builder().baseUrl(base).client(http).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
}
