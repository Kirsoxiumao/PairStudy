package com.pairstudy.app.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairstudy.app.data.model.Checkin
import com.pairstudy.app.data.repository.StudyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class FeedState(val items: List<Checkin> = emptyList(), val loading: Boolean = false, val error: String? = null, val page: Int = 0, val total: Long = 0, val overallTotal: Long = 0, val hasMore: Boolean = false)
class FeedViewModel(private val repo: StudyRepository, private val report: suspend (Exception) -> Unit) : ViewModel() {
    private val mutable = MutableStateFlow(FeedState()); val state = mutable.asStateFlow()
    private var job: Job? = null
    private var date: String? = null; private var category: Long? = null; private var author = "all"
    fun reload(date: String? = null, category: Long? = null, author: String = "all") {
        job?.cancel(); this.date = date; this.category = category; this.author = author
        mutable.value = FeedState(); load()
    }
    fun load() {
        if (mutable.value.loading) return
        job = viewModelScope.launch {
            mutable.update { it.copy(loading = true, error = null) }
            try {
                val page = repo.feed(mutable.value.page + 1, date, category, author)
                val overall = if (category != null && author != "all" && page.page == 1) repo.feed(1, null, category, "all").total else if (author == "all") page.total else mutable.value.overallTotal
                mutable.update { it.copy(items = (it.items + page.items).distinctBy { c -> c.id }, page = page.page, total = page.total, overallTotal = overall, hasMore = page.hasMore) }
            } catch (e: CancellationException) { throw e } catch (e: Exception) {
                mutable.update { it.copy(error = "加载失败，点击重试") }; report(e)
            } finally { mutable.update { it.copy(loading = false) } }
        }
    }
}
