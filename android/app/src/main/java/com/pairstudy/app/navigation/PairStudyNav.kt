package com.pairstudy.app.navigation
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.pairstudy.app.ui.*
import com.pairstudy.app.ui.screen.auth.*
import com.pairstudy.app.ui.screen.home.HomeScreen
import com.pairstudy.app.ui.screen.calendar.*
import com.pairstudy.app.ui.screen.category.*
import com.pairstudy.app.ui.screen.checkin.CreateCheckinScreen
import com.pairstudy.app.ui.screen.profile.ProfileScreen

@Composable private fun feedModel(vm: StudyViewModel, key: String): FeedViewModel = viewModel(key = key, factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = FeedViewModel(vm.repository, vm::handle) as T
})
@Composable fun PairStudyNav(vm: StudyViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm) { vm.messages.collect { snackbar.showSnackbar(it) } }
    val owner = LocalLifecycleOwner.current
    val latestStage by rememberUpdatedState(state.stage)
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME && latestStage in listOf("main", "pair")) vm.refresh() }
        owner.lifecycle.addObserver(observer); onDispose { owner.lifecycle.removeObserver(observer) }
    }
    // Stage changes fully remove the signed-in navigation graph and its ViewModels.
    key(state.stage) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val route = entry?.destination?.route
        fun tab(destination: String) { nav.navigate(destination) { popUpTo("home") { saveState = destination != "create" }; launchSingleTop = true; restoreState = destination != "create" } }
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
            if (state.stage == "main") NavigationBar {
                val tabs = listOf("home" to "首页", "calendar" to "日历", "create" to "打卡", "categories" to "分区", "profile" to "我的")
                val icons = listOf(Icons.Rounded.Home, Icons.Rounded.CalendarMonth, Icons.Rounded.AddCircle, Icons.AutoMirrored.Rounded.MenuBook, Icons.Rounded.Person)
                tabs.forEachIndexed { index, (destination, label) -> NavigationBarItem(selected = route == destination, onClick = { if (destination == "create" && route != "create") vm.discardDraft(); tab(destination) }, enabled = !state.busy, icon = { Icon(icons[index], label, Modifier.size(if (destination == "create") 36.dp else 24.dp)) }, label = { Text(label) }) }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (state.stage) {
                    "auth" -> AuthScreen(state, vm)
                    "pair" -> PairScreen(state, vm)
                    "main" -> NavHost(navController = nav, startDestination = "home") {
                        composable("home") { HomeScreen(state, vm, feedModel(vm, "home")) }
                        composable("calendar") { CalendarScreen(state, vm) { nav.navigate("day/$it") } }
                        composable("create") { CreateCheckinScreen(state, vm, { tab("categories") }, { tab("home") }) }
                        composable("categories") { CategoryScreen(state, vm) { nav.navigate("category/$it") } }
                        composable("profile") { ProfileScreen(state, vm) }
                        composable("day/{date}") { back -> DayDetailScreen(back.arguments?.getString("date") ?: "", state, vm, feedModel(vm, "daySelf"), feedModel(vm, "dayPartner")) { nav.popBackStack() } }
                        composable("category/{id}") { back -> CategoryDetailScreen(back.arguments?.getString("id")?.toLongOrNull() ?: 0, state, vm, feedModel(vm, "category")) { nav.popBackStack() } }
                    }
                    else -> Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state.busy) CircularProgressIndicator() else {
                            Text(state.error ?: "连接你的学习空间")
                            Button(onClick = vm::bootstrap) { Text("重试") }
                            TextButton(onClick = vm::logout) { Text("返回登录") }
                        }
                    }
                }
                if (state.busy && state.stage == "main") LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}
