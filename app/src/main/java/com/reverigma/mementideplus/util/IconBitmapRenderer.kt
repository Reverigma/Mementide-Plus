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
 * 渲染后会自动裁剪掉空白边缘（Material 图标 viewport 有边距，裁剪后内容紧凑，
 * 海报按内容等比缩放时才能与卡片观感一致、不偏移）。
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
        cropToContent(bmp)
    }
}

/** 按非透明像素边界裁剪位图空白边缘；图标内容紧凑，等比缩放时比例/居中准确 */
private fun cropToContent(bmp: Bitmap): Bitmap {
    val w = bmp.width
    val h = bmp.height
    if (w <= 0 || h <= 0) return bmp
    var minX = w
    var minY = h
    var maxX = -1
    var maxY = -1
    val pixels = IntArray(w * h)
    bmp.getPixels(pixels, 0, w, 0, 0, w, h)
    for (y in 0 until h) {
        val row = y * w
        for (x in 0 until w) {
            // alpha > 8 视为有效像素（抗锯齿边缘）
            if ((pixels[row + x] ushr 24) and 0xFF > 8) {
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
            }
        }
    }
    if (maxX < minX) return bmp
    return Bitmap.createBitmap(bmp, minX, minY, maxX - minX + 1, maxY - minY + 1)
}
