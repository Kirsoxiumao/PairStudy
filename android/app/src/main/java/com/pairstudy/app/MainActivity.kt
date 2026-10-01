package com.pairstudy.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pairstudy.app.navigation.PairStudyNav
import com.pairstudy.app.ui.theme.PairStudyTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent { PairStudyTheme { PairStudyNav() } }
    }
}
