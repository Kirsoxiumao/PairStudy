package com.pairstudy.app.ui.screen.profile
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
@Composable fun ProfileScreen(state: StudyState, vm: StudyViewModel) {
    var confirm by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { SectionTitle("我的成长手账", "认真生活，也认真记录") }
        item {
            state.profile?.let { profile ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Avatar(profile.user.nickname, profile.user.avatar)
                    Column { Text(profile.user.nickname, style = MaterialTheme.typography.headlineSmall); Text("@${profile.user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                Spacer(Modifier.height(20.dp)); Text("学习搭子：${profile.partner.nickname}"); Text("相伴开始于 ${profile.boundAt.replace('T', ' ').take(16)}", style = MaterialTheme.typography.bodySmall)
            } ?: Text("正在获取个人信息…")
        }
        item {
            state.stats?.let { stats ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("我们共同坚持了", style = MaterialTheme.typography.titleMedium)
                        Text("${stats.togetherDays} 天", style = MaterialTheme.typography.displaySmall)
                        Text("双方都留下记录的学习日，才算共同完成。", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(stats.totalCheckins to "总打卡次数", stats.monthDays to "本月完成天数", stats.currentStreak to "当前连续天数").forEach { (value, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value.toString(), style = MaterialTheme.typography.headlineMedium); Text(label, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
        item { HorizontalDivider(); Spacer(Modifier.height(12.dp)); Text("今天尚未完成时，连续天数暂按昨天往前计算。学习日以北京时间 04:00 为界，同一天多次打卡只计算一天。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { OutlinedButton(onClick = vm::refresh, enabled = !state.busy) { Text("刷新统计") }; TextButton(onClick = { confirm = true }, enabled = !state.busy) { Text("退出登录") } }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("退出当前账号？") }, text = { Text("已发布的记录会保留在你们的学习空间。") }, confirmButton = { TextButton(onClick = { confirm = false; vm.logout() }) { Text("退出") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } })
}
