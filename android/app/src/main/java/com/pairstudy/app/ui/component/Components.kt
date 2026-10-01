package com.pairstudy.app.ui.component
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pairstudy.app.data.api.ServerConfig
import com.pairstudy.app.data.model.Checkin
import com.pairstudy.app.ui.FeedState
import com.pairstudy.app.ui.theme.StudyColors

fun imageUrl(path: String) = if (path.startsWith("/")) ServerConfig.url.trimEnd('/') + path else path

@Composable fun Avatar(name: String, url: String = "", self: Boolean = true) {
    Box(Modifier.size(42.dp).clip(CircleShape).background(if (self) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
        if (url.isBlank()) Text(name.take(1), style = MaterialTheme.typography.titleMedium)
        else AsyncImage(imageUrl(url), "头像", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}
@Composable fun CharacterSlot(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val id = remember(name) { context.resources.getIdentifier(name, "drawable", context.packageName).let { if (it != 0) it else context.resources.getIdentifier("placeholder_character", "drawable", context.packageName) } }
    if (id != 0) Image(painterResource(id), "陪伴角色", modifier, contentScale = ContentScale.Fit)
    else Box(modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(Icons.Rounded.Spa, "学习伙伴占位图", Modifier.size(44.dp), tint = StudyColors.Primary)
    }
}
@Composable fun PairCircle(self: Boolean, partner: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp).semantics { contentDescription = "你${if (self) "已" else "未"}打卡，朋友${if (partner) "已" else "未"}打卡" }) {
        val stroke = 1.3.dp.toPx(); val inset = stroke / 2
        val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
        val start = androidx.compose.ui.geometry.Offset(inset, inset)
        if (self) drawArc(StudyColors.UserSelf, 90f, 180f, true, start, arcSize)
        if (partner) drawArc(StudyColors.UserPartner, -90f, 180f, true, start, arcSize)
        drawCircle(StudyColors.Primary.copy(alpha = .65f), radius = (size.minDimension - stroke) / 2, style = Stroke(stroke))
        drawLine(StudyColors.Primary.copy(alpha = .5f), androidx.compose.ui.geometry.Offset(size.width / 2, inset), androidx.compose.ui.geometry.Offset(size.width / 2, size.height - inset), stroke)
    }
}
@Composable fun EmptyState(message: String = "这一天还没有留下学习记录。") {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CharacterSlot("character_empty", Modifier.size(68.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun SectionTitle(title: String, subtitle: String? = null, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        trailing?.invoke()
    }
}
@Composable fun CheckinCard(item: Checkin, selfId: Long, onDelete: (Long) -> Unit) {
    var preview by remember { mutableStateOf<Int?>(null) }
    var confirm by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, StudyColors.Divider)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(item.nickname, item.avatar, item.userId == selfId)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("${item.nickname} · ${if (item.userId == selfId) "你" else "搭子"} (${item.role})", style = MaterialTheme.typography.titleSmall)
                    Text("学习日 ${item.effectiveDate}", style = MaterialTheme.typography.labelSmall, color = StudyColors.TextSecondary)
                }
                if (item.userId == selfId) IconButton(onClick = { confirm = true }) { Icon(Icons.Rounded.DeleteOutline, "删除打卡", tint = StudyColors.TextSecondary) }
            }
            if (item.content.isNotBlank()) Text(item.content, style = MaterialTheme.typography.bodyLarge)
            val columns = when (item.images.size) { 1 -> 1; in 2..4 -> 2; else -> 3 }
            item.images.chunked(columns).forEachIndexed { row, images ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    images.forEachIndexed { col, image -> AsyncImage(imageUrl(image.imageUrl), "打卡图片 ${row * columns + col + 1}", Modifier.weight(1f).aspectRatio(if (columns == 1) 1.4f else 1f).clip(RoundedCornerShape(12.dp)).background(StudyColors.Divider).clickable { preview = row * columns + col }, contentScale = ContentScale.Crop) }
                    repeat(columns - images.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) { Text(item.categoryName, Modifier.padding(horizontal = 9.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium) }
                Text(item.createdAt.replace('T', ' ').take(16), style = MaterialTheme.typography.labelSmall, color = StudyColors.TextSecondary)
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("删除这条打卡？") }, text = { Text("删除后会重新计算签到和连续天数，无法撤销。") }, confirmButton = { TextButton(onClick = { confirm = false; onDelete(item.id) }) { Text("删除") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } })
    preview?.let { index ->
        Dialog(onDismissRequest = { preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { preview = null }) { Text("关闭") }
                        Text("${index + 1} / ${item.images.size}", Modifier.padding(12.dp))
                    }
                    AsyncImage(imageUrl(item.images[index].imageUrl), "图片预览", Modifier.weight(1f).fillMaxWidth(), contentScale = ContentScale.Fit)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(enabled = index > 0, onClick = { preview = index - 1 }) { Text("上一张") }
                        TextButton(enabled = index < item.images.lastIndex, onClick = { preview = index + 1 }) { Text("下一张") }
                    }
                }
            }
        }
    }
}
@Composable fun FeedFooter(state: FeedState, load: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
        when { state.loading -> CircularProgressIndicator(Modifier.size(28.dp)); state.error != null -> TextButton(onClick = load) { Text(state.error) }; state.hasMore -> OutlinedButton(onClick = load) { Text("加载更多") }; state.items.isNotEmpty() -> Text("记录到这里，学习还在继续", color = StudyColors.TextSecondary, style = MaterialTheme.typography.labelMedium) }
    }
}
