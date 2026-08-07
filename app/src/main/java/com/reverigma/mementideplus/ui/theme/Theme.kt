package com.reverigma.mementideplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * 液态玻璃（Liquid Glass）风 M3 色板：
 * - 主色靛蓝，容器色一律半透明白（浅色）/ 极低透明度白玻璃（深色），配合页面渐变背景形成玻璃质感
 * - 文本色保持高对比，保证半透明玻璃上可读
 */

private val Primary = Color(0xFF4F46E5)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFE0E7FF)
private val OnPrimaryContainer = Color(0xFF312E81)

private val Secondary = Color(0xFF0D9488)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFCCFBF1)
private val OnSecondaryContainer = Color(0xFF134E4A)

private val Tertiary = Color(0xFFC026D3)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFAE8FF)
private val OnTertiaryContainer = Color(0xFF701A75)

private val Error = Color(0xFFDC2626)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFE4E6)
private val OnErrorContainer = Color(0xFF991B1B)

// 白透风格（浅色）：背景纯白系，surface 为白色毛玻璃（顶栏/导航栏），文字深灰保证对比
private val Background = Color(0xFFFAFBFE)
private val OnBackground = Color(0xFF1C1B1F)
private val Surface = Color(0xD9FFFFFF)          // 85% 白玻璃（顶栏/导航栏，白透不露黑窗）
private val OnSurface = Color(0xFF1C1B1F)
private val SurfaceVariant = Color(0xF2FFFFFF)   // 95% 白玻璃
private val OnSurfaceVariant = Color(0xFF5A6072)
private val Outline = Color(0xFFE5E7EB)
private val OutlineVariant = Color(0xFFF3F4F6)
private val InverseSurface = Color(0xFF313033)
private val InverseOnSurface = Color(0xFFF4EFF4)
private val InversePrimary = Color(0xFFC7D2FE)
private val SurfaceTint = Primary

// 容器色：白系常规色（卡片玻璃由 GlassCard 实现，其余组件白底深字）
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF8F9FB)
private val SurfaceContainer = Color(0xFFF2F4F7)
private val SurfaceContainerHigh = Color(0xFFECEEF2)
private val SurfaceContainerHighest = Color(0xFFE5E7EB)

private val DarkPrimary = Color(0xFFC7D2FE)
private val DarkOnPrimary = Color(0xFF312E81)
private val DarkPrimaryContainer = Color(0xFF4338CA)
private val DarkOnPrimaryContainer = Color(0xFFE0E7FF)

private val DarkSecondary = Color(0xFF5EEAD4)
private val DarkOnSecondary = Color(0xFF134E4A)
private val DarkSecondaryContainer = Color(0xFF115E59)
private val DarkOnSecondaryContainer = Color(0xFFCCFBF1)

private val DarkTertiary = Color(0xFFF0ABFC)
private val DarkOnTertiary = Color(0xFF701A75)
private val DarkTertiaryContainer = Color(0xFF86198F)
private val DarkOnTertiaryContainer = Color(0xFFFAE8FF)

private val DarkError = Color(0xFFFFA1A1)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

// 白透风格（深色）：背景深灰（不彩），surface 为暗色玻璃（顶栏/导航栏）
private val DarkBackground = Color(0xFF14151A)
private val DarkOnBackground = Color(0xFFE5E1E6)
private val DarkSurface = Color(0x73000000)          // 45% 暗玻璃
private val DarkOnSurface = Color(0xFFE5E1E6)
private val DarkSurfaceVariant = Color(0x14FFFFFF)   // 8% 白玻璃
private val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
private val DarkOutline = Color(0xFF4B5563)
private val DarkOutlineVariant = Color(0xFF374151)
private val DarkInverseSurface = Color(0xFFE5E1E6)
private val DarkInverseOnSurface = Color(0xFF313033)
private val DarkInversePrimary = Color(0xFF4F46E5)
private val DarkSurfaceTint = DarkPrimary

// 深色容器：深灰常规色（卡片玻璃由 GlassCard 实现）
private val DarkSurfaceContainerLowest = Color(0xFF0E0F13)
private val DarkSurfaceContainerLow = Color(0xFF1C1D22)
private val DarkSurfaceContainer = Color(0xFF2A2C31)
private val DarkSurfaceContainerHigh = Color(0xFF35373C)
private val DarkSurfaceContainerHighest = Color(0xFF404247)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    surfaceTint = SurfaceTint,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    surfaceTint = DarkSurfaceTint,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

@Composable
fun MementideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
