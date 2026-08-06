package com.reverigma.mementideplus.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.Yard
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Material Icon 图标目录：图标名 -> ImageVector。
 * 用于习惯 / 纪念日的图标选择（风格统一、与 M3 设计语言一致）。
 */
object IconCatalog {

    /** 所有可选图标（name -> vector） */
    val ICONS: Map<String, ImageVector> = mapOf(
        "star" to Icons.Outlined.Star,
        "favorite" to Icons.Outlined.Favorite,
        "fitness" to Icons.Outlined.FitnessCenter,
        "run" to Icons.Outlined.DirectionsRun,
        "bike" to Icons.Outlined.DirectionsBike,
        "pool" to Icons.Outlined.Pool,
        "book" to Icons.Outlined.Book,
        "school" to Icons.Outlined.School,
        "work" to Icons.Outlined.Work,
        "code" to Icons.Outlined.Code,
        "language" to Icons.Outlined.Language,
        "music" to Icons.Outlined.MusicNote,
        "palette" to Icons.Outlined.Palette,
        "brush" to Icons.Outlined.Brush,
        "spa" to Icons.Outlined.Spa,
        "flower" to Icons.Outlined.LocalFlorist,
        "yard" to Icons.Outlined.Yard,
        "plant" to Icons.Outlined.Yard,
        "sun" to Icons.Outlined.WbSunny,
        "light" to Icons.Outlined.Lightbulb,
        "coffee" to Icons.Outlined.LocalCafe,
        "food" to Icons.Outlined.Restaurant,
        "cake" to Icons.Outlined.Cake,
        "gift" to Icons.Outlined.CardGiftcard,
        "flight" to Icons.Outlined.Flight,
        "meditation" to Icons.Outlined.SelfImprovement,
        "accessibility" to Icons.Outlined.Accessibility,
        "thumb" to Icons.Outlined.ThumbUp,
        "pet" to Icons.Outlined.Pets
    )

    /** 根据图标名取 ImageVector，找不到返回 null */
    fun vector(name: String): ImageVector? = ICONS[name]

    /** 常用推荐图标（选择器展示顺序） */
    val DEFAULT_NAMES: List<String> = listOf(
        "star", "favorite", "fitness", "run", "bike", "pool",
        "book", "school", "work", "code", "language", "music",
        "palette", "brush", "spa", "flower", "sun", "light",
        "coffee", "food", "cake", "gift", "flight", "meditation",
        "accessibility", "thumb", "pet"
    )
}
