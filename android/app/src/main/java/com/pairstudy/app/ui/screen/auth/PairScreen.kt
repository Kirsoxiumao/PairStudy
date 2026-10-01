package com.pairstudy.app.ui.screen.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.CharacterSlot
@Composable fun PairScreen(state: StudyState, vm: StudyViewModel) {
    var code by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(24.dp)); CharacterSlot("character_home", Modifier.size(84.dp))
        Text("邀请你的学习搭子", style = MaterialTheme.typography.headlineMedium)
        Text("这个空间，只属于你们两个人。\n一人生成邀请码，另一人输入即可。")
        state.pair?.inviteCode?.let { SelectionContainer { Text(it, style = MaterialTheme.typography.headlineSmall) }; Text("长按复制，发给你的朋友。", style = MaterialTheme.typography.bodySmall) }
        Button(onClick = vm::invite, enabled = !state.busy) { Text("生成 / 查看我的邀请码") }
        HorizontalDivider(); OutlinedTextField(code, { code = it.trim().uppercase().take(16) }, Modifier.fillMaxWidth(), label = { Text("输入朋友的 16 位邀请码") }, singleLine = true)
        Button(onClick = { vm.bind(code) }, enabled = !state.busy && code.length == 16) { Text("绑定搭子") }
        OutlinedButton(onClick = vm::refresh, enabled = !state.busy) { Text("朋友已绑定，刷新进入") }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(onClick = vm::logout, enabled = !state.busy) { Text("退出账号") }
    }
}
