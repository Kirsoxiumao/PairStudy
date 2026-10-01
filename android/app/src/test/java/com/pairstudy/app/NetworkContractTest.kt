package com.pairstudy.app
import com.pairstudy.app.data.api.*
import com.pairstudy.app.data.model.*
import com.pairstudy.app.data.repository.*
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*

class NetworkContractTest {
    private lateinit var server: MockWebServer
    @Before fun start() { server = MockWebServer(); server.start(); ServerConfig.set(server.url("/").toString()) }
    @After fun stop() { server.shutdown(); ServerConfig.set(BuildConfig.BASE_URL) }
    @Test fun apiUsesConfiguredServerAndToken() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody("""{"code":200,"message":"success","data":{"groupId":null,"userA":null,"userB":null,"inviteCode":null,"createdAt":null,"boundAt":null,"effectiveDate":"2026-10-01","serverNow":"2026-10-01T00:00:00Z","nextResetAt":"2026-10-01T20:00:00Z"}}"""))
        val result = RetrofitClient { "test-token" }.api.pair()
        assertEquals("2026-10-01", result.data?.effectiveDate)
        val request = server.takeRequest()
        assertEquals("/api/pair/info", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
    }
    @Test fun externalImageNeverReceivesToken() {
        MockWebServer().use { other ->
            other.start(); other.enqueue(MockResponse().setBody("external"))
            RetrofitClient { "private-token" }.http.newCall(Request.Builder().url(other.url("/image.png")).build()).execute().use { assertTrue(it.isSuccessful) }
            assertNull(other.takeRequest().getHeader("Authorization"))
        }
    }
    @Test fun protectedImageReceivesToken() {
        server.enqueue(MockResponse().setBody("image"))
        RetrofitClient { "private-token" }.http.newCall(Request.Builder().url(server.url("/uploads/checkin/a.jpg")).build()).execute().close()
        assertEquals("Bearer private-token", server.takeRequest().getHeader("Authorization"))
    }
    @Test fun rejectCredentialsAndApiSuffixInBaseUrl() {
        for (url in listOf("not a url", "http://user:password@example.com/", "http://example.com/api/")) {
            assertThrows(IllegalArgumentException::class.java) { ServerConfig.set(url) }
        }
    }
}
