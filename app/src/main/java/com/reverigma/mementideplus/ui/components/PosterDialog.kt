package com.reverigma.mementideplus.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 海报预览对话框：全屏展示生成的海报图，右上角分享按钮。
 * @param bitmap 生成的海报位图
 * @param onShare 点击分享按钮回调（由调用方触发保存 + 系统分享）
 * @param onDismiss 关闭对话框
 */
@Composable
fun PosterDialog(
    bitmap: Bitmap,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Color.White,
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "海报",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            // 右上角分享按钮（浮在海报上，白底圆）
            Surface(
                shape = CircleShape,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "分享",
                        tint = androidx.compose.ui.graphics.Color(0xFF1F2937),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
