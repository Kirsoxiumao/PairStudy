package com.pairstudy.app.ui.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
object StudyColors {
    val Primary = Color(0xFF547767)
    val Secondary = Color(0xFFA47A69)
    val Background = Color(0xFFF8F7F3)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF283C33)
    val TextSecondary = Color(0xFF737B75)
    val Divider = Color(0xFFE1E6DE)
    val UserSelf = Color(0xFF749B84)
    val UserPartner = Color(0xFFD8A38A)
    val Success = Color(0xFF47765A)
}
@Composable fun PairStudyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = StudyColors.Primary, secondary = StudyColors.Secondary,
        background = StudyColors.Background, surface = StudyColors.Surface,
        onBackground = StudyColors.TextPrimary, onSurface = StudyColors.TextPrimary,
        onSurfaceVariant = StudyColors.TextSecondary, outlineVariant = StudyColors.Divider,
        primaryContainer = Color(0xFFE5EFE5), secondaryContainer = Color(0xFFF7E9E1)
    ), content = content)
}
