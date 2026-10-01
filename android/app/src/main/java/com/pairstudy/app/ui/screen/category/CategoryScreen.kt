package com.pairstudy.app.ui.screen.category
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import com.pairstudy.app.data.model.Category
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.*
@Composable fun CategoryScreen(state: StudyState, vm: StudyViewModel, open: (Long) -> Unit) {
    var editor by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Category?>(null) }
    var name by remember { mutableStateOf("") }; var archive by remember { mutableStateOf<Category?>(null) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { SectionTitle("我们的学习分区", "一起整理知识，一起拓宽世界") }
        item { Row { Button(onClick = { editing = null; name = ""; editor = true }, enabled = !state.busy) { Icon(Icons.Rounded.Add, null); Text("新建分区") }; Spacer(Modifier.width(8.dp)); TextButton(onClick = vm::refresh, enabled = !state.busy) { Text("刷新") } } }
        if (state.categories.isEmpty()) item { EmptyState("给第一个学习目标，起个名字吧。") }
        items(state.categories, key = { it.id }) { category ->
            Card(Modifier.fillMaxWidth().clickable { open(category.id) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(12.dp))
                        Text(category.name + if (category.archived) " · 已归档" else "", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, "查看历史")
                    }
                    if (!category.archived) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { vm.moveCategory(category, -1) }, enabled = !state.busy) { Icon(Icons.Rounded.ArrowUpward, "上移") }
                        IconButton(onClick = { vm.moveCategory(category, 1) }, enabled = !state.busy) { Icon(Icons.Rounded.ArrowDownward, "下移") }
                        IconButton(onClick = { editing = category; name = category.name; editor = true }, enabled = !state.busy) { Icon(Icons.Rounded.Edit, "改名") }
                        IconButton(onClick = { archive = category }, enabled = !state.busy) { Icon(Icons.Rounded.Archive, "归档分区") }
                    }
                }
            }
        }
    }
    if (editor) AlertDialog(onDismissRequest = { editor = false }, title = { Text(if (editing == null) "新建共享分区" else "修改分区") }, text = { OutlinedTextField(name, { name = it.take(40) }, singleLine = true, label = { Text("分区名称") }) }, confirmButton = { TextButton(onClick = { vm.saveCategory(editing?.id, name, editing?.sortOrder ?: ((state.categories.maxOfOrNull { it.sortOrder } ?: 0) + 10)); editor = false }, enabled = name.isNotBlank() && !state.busy) { Text("保存") } }, dismissButton = { TextButton(onClick = { editor = false }) { Text("取消") } })
    archive?.let { c -> AlertDialog(onDismissRequest = { archive = null }, title = { Text("归档「${c.name}」？") }, text = { Text("不再用于新打卡，历史记录仍可查看。") }, confirmButton = { TextButton(onClick = { vm.archive(c); archive = null }) { Text("归档") } }, dismissButton = { TextButton(onClick = { archive = null }) { Text("取消") } }) }
}
