package com.pairstudy.app.ui.screen.auth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.pairstudy.app.R
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.component.CharacterSlot
@Composable fun AuthScreen(state: StudyState, vm: StudyViewModel) {
    var register by rememberSaveable { mutableStateOf(false) }; var username by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }; var nickname by rememberSaveable { mutableStateOf("") }
    var serverDialog by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf(com.pairstudy.app.data.api.ServerConfig.url) }
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(36.dp)); CharacterSlot("character_home", Modifier.size(84.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall)
        Text("两个人的小小学习空间\n把每一天的努力，留给彼此看见。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(if (register) "开始一起成长" else "欢迎回来", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(username, { username = it.take(32) }, Modifier.fillMaxWidth(), label = { Text("用户名 · 3–32 位字母、数字或下划线") }, singleLine = true)
        if (register) OutlinedTextField(nickname, { nickname = it.take(40) }, Modifier.fillMaxWidth(), label = { Text("昵称") }, singleLine = true)
        OutlinedTextField(password, { password = it.take(64) }, Modifier.fillMaxWidth(), label = { Text("密码 · 至少 8 位") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = { vm.auth(register, username, password, nickname) }, enabled = !state.busy && username.matches(Regex("[A-Za-z0-9_]{3,32}")) && password.length >= 8 && (!register || nickname.isNotBlank()), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (state.busy) "正在连接…" else if (register) "创建账号" else "登录") }
        TextButton(onClick = { register = !register }, enabled = !state.busy) { Text(if (register) "已有账号？去登录" else "第一次来？创建账号") }
        Text("学习日每天凌晨 04:00 更新", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { serverUrl = com.pairstudy.app.data.api.ServerConfig.url; serverDialog = true }, enabled = !state.busy) { Text("设置服务器地址") }
    }
    if (serverDialog) AlertDialog(onDismissRequest = { serverDialog = false }, title = { Text("连接电脑上的后端") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("手机与电脑连接同一 WiFi，填写电脑的局域网 IPv4。例如 http://192.168.1.100:8080/")
            OutlinedTextField(serverUrl, { serverUrl = it }, singleLine = true, label = { Text("服务器地址") })
        }
    }, confirmButton = { TextButton(onClick = { vm.configureServer(serverUrl); serverDialog = false }) { Text("保存") } }, dismissButton = { TextButton(onClick = { serverDialog = false }) { Text("取消") } })
}
