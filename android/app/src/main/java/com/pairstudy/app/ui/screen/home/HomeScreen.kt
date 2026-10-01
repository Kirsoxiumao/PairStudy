package com.pairstudy.app.ui.screen.home
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.pairstudy.app.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
import java.time.LocalDate
@Composable fun HomeScreen(state: StudyState, vm: StudyViewModel, feed: FeedViewModel) {
    val stream by feed.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.revision) { feed.reload() }
    val today = state.pair?.effectiveDate
    val current = state.todayStatus
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { SectionTitle(stringResource(R.string.app_name), "今天是 ${today ?: "…"} · 学习日") { IconButton(onClick = vm::refresh, enabled = !state.busy) { Icon(Icons.Rounded.Refresh, "刷新") } } }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("一起，把今天学好。", style = MaterialTheme.typography.titleLarge)
                        Text("每一点进步，都值得被看见。", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Text("你 ${if (current == null) "…" else if (current.selfChecked) "● 已打卡" else "○ 未打卡"}", style = MaterialTheme.typography.labelLarge)
                            Text("搭子 ${if (current == null) "…" else if (current.partnerChecked) "● 已打卡" else "○ 未打卡"}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    CharacterSlot("character_home", Modifier.size(68.dp))
                }
            }
        }
        item { SectionTitle("我们的学习手记", "每日 04:00 开启新的学习日") }
        if (stream.items.isEmpty() && !stream.loading && stream.error == null) item { EmptyState("第一篇学习手记，等你们来写。") }
        items(stream.items, key = { it.id }) { CheckinCard(it, vm.userId, vm::delete) }
        item { FeedFooter(stream, feed::load) }
    }
}
