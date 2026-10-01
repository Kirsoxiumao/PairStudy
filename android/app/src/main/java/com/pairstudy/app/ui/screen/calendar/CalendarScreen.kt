package com.pairstudy.app.ui.screen.calendar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
@Composable fun CalendarScreen(state: StudyState, vm: StudyViewModel, openDay: (String) -> Unit) {
    val month = state.month ?: return
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionTitle("一起坚持的日子", "左半边是你，右半边是搭子") }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { vm.changeMonth(-1) }, enabled = !state.busy) { Text("‹ 上月") }
                Text("${month.year} 年 ${month.monthValue} 月", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { vm.changeMonth(1) }, enabled = !state.busy) { Text("下月 ›") }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row { listOf("一", "二", "三", "四", "五", "六", "日").forEach { Box(Modifier.weight(1f).height(36.dp), contentAlignment = Alignment.Center) { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
                    val leading = month.atDay(1).dayOfWeek.value - 1
                    val cells = (List<Int?>(leading) { null } + (1..month.lengthOfMonth()).toList()).chunked(7)
                    cells.forEach { week ->
                        Row {
                            week.forEach { day ->
                                if (day == null) Spacer(Modifier.weight(1f)) else {
                                    val date = month.atDay(day).toString(); val status = state.days.firstOrNull { it.date == date }
                                    Surface(Modifier.weight(1f).padding(2.dp).clickable { openDay(date) }, shape = RoundedCornerShape(10.dp), color = if (date == state.pair?.effectiveDate) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                                        Column(Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(day.toString(), style = MaterialTheme.typography.bodyMedium)
                                            if (status != null) PairCircle(status.selfChecked, status.partnerChecked) else Text("·")
                                        }
                                    }
                                }
                            }
                            repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(false to false, true to false, false to true, true to true).zip(listOf("尚未打卡", "只有你", "只有搭子", "共同完成")).forEach { (pair, label) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { PairCircle(pair.first, pair.second); Text(label, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        item { CharacterSlot("character_calendar", Modifier.fillMaxWidth().height(88.dp)) }
        item { Text("点击日期，看看那天的努力。凌晨 00:00–03:59 发布的记录属于前一个学习日。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        item { OutlinedButton(onClick = vm::refresh, enabled = !state.busy) { Text("刷新日历") } }
    }
}
