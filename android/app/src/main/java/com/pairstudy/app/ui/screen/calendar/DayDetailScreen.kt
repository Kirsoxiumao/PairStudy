package com.pairstudy.app.ui.screen.calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
@Composable fun DayDetailScreen(date: String, state: StudyState, vm: StudyViewModel, selfFeed: FeedViewModel, partnerFeed: FeedViewModel, back: () -> Unit) {
    val mine by selfFeed.state.collectAsStateWithLifecycle(); val partner by partnerFeed.state.collectAsStateWithLifecycle()
    LaunchedEffect(date, state.revision) { selfFeed.reload(date = date, author = "self"); partnerFeed.reload(date = date, author = "partner") }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = back) { Text("‹ 返回日历") }; SectionTitle(date, "这一天，我们留下的足迹") }
        item { Text("你的打卡 · ${mine.total} 条", style = MaterialTheme.typography.titleMedium) }
        if (mine.items.isEmpty() && !mine.loading && mine.error == null) item { EmptyState() }
        items(mine.items, key = { "self${it.id}" }) { CheckinCard(it, vm.userId, vm::delete) }
        item { FeedFooter(mine, selfFeed::load); HorizontalDivider() }
        item { Text("搭子的打卡 · ${partner.total} 条", style = MaterialTheme.typography.titleMedium) }
        if (partner.items.isEmpty() && !partner.loading && partner.error == null) item { EmptyState() }
        items(partner.items, key = { "partner${it.id}" }) { CheckinCard(it, vm.userId, vm::delete) }
        item { FeedFooter(partner, partnerFeed::load) }
    }
}
