package com.pairstudy.app.data.api
import com.pairstudy.app.BuildConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
object ServerConfig {
    @Volatile var url: String = BuildConfig.BASE_URL; private set
    fun set(value: String) {
        val parsed = value.trim().toHttpUrlOrNull() ?: throw IllegalArgumentException("请输入完整的 http(s) 服务器地址")
        require(parsed.encodedPath == "/" && parsed.query == null && parsed.username.isEmpty() && parsed.password.isEmpty()) { "地址只需填写协议、IP 和端口，以 / 结尾" }
        require(BuildConfig.DEBUG || parsed.isHttps) { "Release 版本必须使用 HTTPS" }
        url = parsed.toString()
    }
}
