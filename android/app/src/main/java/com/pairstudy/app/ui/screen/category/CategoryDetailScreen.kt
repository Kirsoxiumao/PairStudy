package com.pairstudy.app.ui.screen.category
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
@Composable fun CategoryDetailScreen(id: Long, state: StudyState, vm: StudyViewModel, feed: FeedViewModel, back: () -> Unit) {
    var author by rememberSaveable(id) { mutableStateOf("all") }; val stream by feed.state.collectAsStateWithLifecycle()
    LaunchedEffect(id, author, state.revision) { feed.reload(category = id, author = author) }
    val category = state.categories.firstOrNull { it.id == id }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = back) { Text("‹ 返回分区") }; SectionTitle(category?.name ?: "分区详情", "总计 ${stream.overallTotal} 条学习记录" + if (author == "all") "" else " · 当前筛选 ${stream.total} 条") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("all" to "全部", "self" to "只看我", "partner" to "只看搭子").forEach { (value, label) -> FilterChip(selected = author == value, onClick = { author = value }, label = { Text(label) }) } } }
        if (stream.items.isEmpty() && !stream.loading && stream.error == null) item { EmptyState("这个分区还没有学习记录。") }
        items(stream.items, key = { it.id }) { CheckinCard(it, vm.userId, vm::delete) }
        item { FeedFooter(stream, feed::load) }
    }
}
