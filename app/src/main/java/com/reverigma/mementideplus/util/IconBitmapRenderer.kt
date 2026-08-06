package com.reverigma.mementideplus.util

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * 在 Compose 上下文把 Material 图标渲染为位图（透明背景），用于海报等纯 Canvas 绘制场景。
 * @param iconName 图标名（IconCatalog 中的 name）
 * @param sizeDp 输出位图边长（dp），建议用较大值保证清晰
 * @param tint 图标颜色
 */
@Composable
fun rememberMaterialIconBitmap(iconName: String, sizeDp: Int, tint: Color): Bitmap? {
    val vector = IconCatalog.vector(iconName) ?: return null
    val density = androidx.compose.ui.platform.LocalDensity.current
    val painter = rememberVectorPainter(image = vector)
    return remember(vector, painter, sizeDp, tint) {
        val px = with(density) { sizeDp.dp.roundToPx() }
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = androidx.compose.ui.graphics.Canvas(bmp.asImageBitmap())
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = density,
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(px.toFloat(), px.toFloat())
        ) {
            with(painter) {
                draw(
                    size = Size(px.toFloat(), px.toFloat()),
                    colorFilter = ColorFilter.tint(tint)
                )
            }
        }
        bmp
    }
}
