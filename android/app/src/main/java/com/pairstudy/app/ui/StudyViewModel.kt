package com.pairstudy.app.ui
import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pairstudy.app.PairStudyApplication
import com.pairstudy.app.data.model.*
import com.pairstudy.app.data.repository.ApiFailure
import com.pairstudy.app.util.ImageCompressor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.*
import java.util.UUID

data class StudyState(
    val stage: String = "starting", val busy: Boolean = false, val error: String? = null,
    val pair: PairInfo? = null, val categories: List<Category> = emptyList(),
    val month: YearMonth? = null, val days: List<CalendarDay> = emptyList(),
    val profile: Profile? = null, val stats: Statistics? = null, val revision: Int = 0,
    val published: Int = 0, val uploadProgress: String = "", val todayStatus: CalendarDay? = null
)
class StudyViewModel(application: Application) : AndroidViewModel(application) {
    val repository = (application as PairStudyApplication).repository
    private val mutable = MutableStateFlow(StudyState())
    val state = mutable.asStateFlow()
    val userId get() = repository.session.userId
    private val notices = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = notices.asSharedFlow()
    private var resetJob: Job? = null
    private val uploaded = mutableMapOf<String, Long>()
    private var draftId = UUID.randomUUID().toString()
    init { bootstrap() }
    private fun action(block: suspend () -> Unit) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.update { it.copy(busy = true, error = null) }
            try { block() } catch (e: CancellationException) { throw e } catch (e: Exception) { handle(e) }
            finally { mutable.update { it.copy(busy = false, uploadProgress = "") } }
        }
    }
    suspend fun handle(e: Exception) {
        val message = if (e is ApiFailure) e.message else "操作失败，请稍后重试"
        if (e is ApiFailure && e.code == 401 && repository.session.token != null) clearSession()
        mutable.update { it.copy(error = message) }; notices.emit(message)
    }
    fun bootstrap() = action {
        repository.session.load()
        if (repository.session.token == null) mutable.update { it.copy(stage = "auth") }
        else refreshData()
    }
    fun auth(register: Boolean, username: String, password: String, nickname: String) = action {
        if (register) repository.register(username.trim(), password, nickname.trim()) else repository.login(username.trim(), password)
        refreshData()
    }
    fun refresh() = action { refreshData() }
    private suspend fun refreshData() {
        val pair = repository.pair()
        val bound = pair.userB != null
        val month = mutable.value.month ?: YearMonth.from(LocalDate.parse(pair.effectiveDate))
        mutable.update { it.copy(pair = pair, stage = if (bound) "main" else "pair", month = month) }
        if (bound) coroutineScope {
            val cats = async { repository.categories() }; val profile = async { repository.profile() }
            val stats = async { repository.statistics() }; val days = async { repository.month(month.year, month.monthValue) }
            val today = LocalDate.parse(pair.effectiveDate)
            val todayStatus = async { repository.month(today.year, today.monthValue).firstOrNull { it.date == pair.effectiveDate } }
            val c = cats.await(); val p = profile.await(); val s = stats.await(); val d = days.await(); val t = todayStatus.await()
            mutable.update { it.copy(categories = c, profile = p, stats = s, days = d, todayStatus = t, revision = it.revision + 1) }
        }
        resetJob?.cancel()
        val delayMs = Duration.between(Instant.parse(pair.serverNow), Instant.parse(pair.nextResetAt)).toMillis().coerceAtLeast(1000)
        resetJob = viewModelScope.launch { delay(delayMs + 500); while (mutable.value.busy) delay(250); refresh() }
    }
    fun invite() = action { val pair = repository.invite(); mutable.update { it.copy(pair = pair) } }
    fun bind(code: String) = action { repository.bind(code.trim()); refreshData() }
    fun changeMonth(delta: Long) = action {
        val month = (mutable.value.month ?: return@action).plusMonths(delta)
        val days = repository.month(month.year, month.monthValue)
        mutable.update { it.copy(month = month, days = days) }
    }
    fun saveCategory(id: Long?, name: String, order: Int) = action { repository.category(id, CategoryInput(name.trim(), sortOrder = order)); refreshData(); notices.emit("分区已保存") }
    fun moveCategory(category: Category, delta: Int) = action {
        // Reassign explicit order values; no client-only ordering.
        val active = mutable.value.categories.filterNot { it.archived }.toMutableList()
        val index = active.indexOfFirst { it.id == category.id }; val next = index + delta
        if (index >= 0 && next in active.indices) {
            java.util.Collections.swap(active, index, next)
            active.forEachIndexed { i, c -> repository.category(c.id, CategoryInput(c.name, c.iconName, i * 10)) }
            refreshData()
        }
    }
    fun archive(category: Category) = action { repository.archive(category.id); refreshData(); notices.emit("分区已归档，历史记录保留") }
    fun publish(category: Long, content: String, uris: List<String>) = action {
        val ids = uris.mapIndexed { i, uri ->
            mutable.update { it.copy(uploadProgress = "正在上传 ${i + 1}/${uris.size}") }
            uploaded[uri] ?: repository.upload(ImageCompressor.compress(getApplication(), uri.toUri())).id.also { uploaded[uri] = it }
        }
        mutable.update { it.copy(uploadProgress = "正在保存记录") }
        repository.publish(CheckinInput(category, content, ids, draftId))
        uploaded.clear(); draftId = UUID.randomUUID().toString()
        mutable.update { it.copy(published = it.published + 1) }
        notices.emit("今日记录完成 ✓")
        refreshData()
    }
    fun discardDraft() { uploaded.clear(); draftId = UUID.randomUUID().toString() }
    fun delete(id: Long) = action { repository.delete(id); refreshData(); notices.emit("记录已删除") }
    fun logout() = action { clearSession() }
    fun configureServer(url: String) = action {
        try { repository.session.server(url); clearSession(); notices.emit("服务器地址已保存") }
        catch (e: IllegalArgumentException) { throw ApiFailure(400, e.message ?: "服务器地址不正确") }
    }
    private suspend fun clearSession() {
        resetJob?.cancel(); repository.session.clear(); uploaded.clear(); draftId = UUID.randomUUID().toString()
        coil.Coil.imageLoader(getApplication()).memoryCache?.clear()
        mutable.value = StudyState(stage = "auth")
    }
}
