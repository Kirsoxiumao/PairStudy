package com.pairstudy.app.ui.screen.checkin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.SectionTitle
@Composable fun CreateCheckinScreen(state: StudyState, vm: StudyViewModel, goCategories: () -> Unit, done: () -> Unit) {
    var content by rememberSaveable { mutableStateOf("") }; var images by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }; var expanded by remember { mutableStateOf(false) }
    val startVersion = remember { state.published }
    LaunchedEffect(state.published) { if (state.published > startVersion) done() }
    val active = state.categories.filterNot { it.archived }
    LaunchedEffect(active) { if (active.none { it.id == selected }) selected = active.firstOrNull()?.id }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(9)) { uris ->
        images = (images + uris.map { it.toString() }).distinct().take(9)
    }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("留下今天的努力", "归属学习日由服务器在发布时确认")
        OutlinedTextField(content, { if (it.length <= 5000) content = it }, Modifier.fillMaxWidth().heightIn(min = 180.dp), enabled = !state.busy, placeholder = { Text("今天学到了什么？也可以只分享图片。") }, supportingText = { Text("${content.length} / 5000") })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images, key = { it }) { uri ->
                Box(Modifier.size(110.dp)) {
                    AsyncImage(uri, "已选图片", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    FilledIconButton(onClick = { images = images - uri }, enabled = !state.busy, modifier = Modifier.align(Alignment.TopEnd).size(32.dp)) { Icon(Icons.Rounded.Close, "移除此图片", Modifier.size(18.dp)) }
                }
            }
        }
        OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !state.busy && images.size < 9) { Icon(Icons.Rounded.AddPhotoAlternate, null); Spacer(Modifier.width(8.dp)); Text("选择图片 ${images.size}/9") }
        if (active.isEmpty()) {
            Text("先创建一个共享学习分区，再开始打卡。")
            TextButton(onClick = goCategories, enabled = !state.busy) { Text("去创建分区") }
        } else Box {
            OutlinedButton(onClick = { expanded = true }, enabled = !state.busy) { Text("分区：${active.firstOrNull { it.id == selected }?.name ?: "请选择"} ▾") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { active.forEach { c -> DropdownMenuItem(text = { Text(c.name) }, onClick = { selected = c.id; expanded = false }) } }
        }
        Text("图片会压缩后上传，最长边约 1600 像素。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { selected?.let { vm.publish(it, content, images.toList()) } }, enabled = !state.busy && selected != null && (content.isNotBlank() || images.isNotEmpty()), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (state.busy) state.uploadProgress.ifBlank { "请稍候…" } else "发布学习记录") }
    }
}
