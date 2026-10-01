package com.pairstudy.app.data.repository
import com.google.gson.Gson
import com.pairstudy.app.data.api.ApiService
import com.pairstudy.app.data.local.SessionStore
import com.pairstudy.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
class ApiFailure(val code: Int, override val message: String) : Exception(message)
class StudyRepository(private val api: ApiService, val session: SessionStore) {
    private suspend fun <T> call(block: suspend () -> ApiResponse<T>): T {
        val result = response(block)
        return result.data ?: throw ApiFailure(500, "服务器返回了空数据，请稍后重试")
    }
    private suspend fun <T> response(block: suspend () -> ApiResponse<T>): ApiResponse<T> = try {
        block().also { if (it.code != 200) throw ApiFailure(it.code, it.message) }
    } catch (e: CancellationException) { throw e
    } catch (e: HttpException) {
        val message = runCatching { Gson().fromJson(e.response()?.errorBody()?.string(), ApiResponse::class.java).message }.getOrNull()
        throw ApiFailure(e.code(), message ?: "请求失败，请稍后重试")
    } catch (e: IOException) { throw ApiFailure(0, "网络连接失败，请检查服务器地址和 WiFi 后重试") }
    suspend fun login(username: String, password: String) { session.save(call { api.login(LoginInput(username, password)) }) }
    suspend fun register(username: String, password: String, nickname: String) { session.save(call { api.register(RegisterInput(username, password, nickname)) }) }
    suspend fun pair() = call { api.pair() }.also { session.group(it.groupId) }
    suspend fun invite() = call { api.invite() }.also { session.group(it.groupId) }
    suspend fun bind(code: String) = call { api.bind(BindInput(code)) }.also { session.group(it.groupId) }
    suspend fun categories() = call { api.categories() }
    suspend fun category(id: Long?, input: CategoryInput) = call { if (id == null) api.createCategory(input) else api.updateCategory(id, input) }
    suspend fun archive(id: Long) { response { api.archiveCategory(id) } }
    suspend fun upload(file: MultipartBody.Part) = call { api.upload(file) }
    suspend fun publish(input: CheckinInput) = call { api.createCheckin(input) }
    suspend fun feed(page: Int, date: String?, category: Long?, author: String): Page<Checkin> = call {
        when { date != null -> api.day(date, page, author); category != null -> api.categoryFeed(category, author, page); else -> api.feed(page) }
    }
    suspend fun delete(id: Long) { response { api.deleteCheckin(id) } }
    suspend fun month(year: Int, month: Int) = call { api.month(year, month) }
    suspend fun profile() = call { api.profile() }
    suspend fun statistics() = call { api.statistics() }
}
