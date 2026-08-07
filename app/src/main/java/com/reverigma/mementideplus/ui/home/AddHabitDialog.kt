package com.reverigma.mementideplus.ui.home

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.ui.components.ColorPickerRow
import com.reverigma.mementideplus.ui.components.IconPicker
import com.reverigma.mementideplus.util.ImageStore

@Composable
fun AddHabitDialog(
    initial: Habit? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String, color: Int, target: Int, imagePath: String) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var iconName by remember(initial?.id) { mutableStateOf(initial?.iconName?.takeIf { it.isNotBlank() } ?: "star") }
    var color by remember(initial?.id) { mutableStateOf(initial?.colorInt ?: 0xFF4F46E5.toInt()) }
    var target by remember(initial?.id) { mutableStateOf(initial?.targetPerWeek ?: 7) }
    var imagePath by remember(initial?.id) { mutableStateOf(initial?.imagePath ?: "") }

    val colors = listOf(
        0xFF4F46E5, 0xFF0EA5E9, 0xFF10B981, 0xFFF59E0B,
        0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6
    ).map { it.toInt() }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ImageStore.saveFromUri(context, uri, "habit_images")?.let { newPath ->
                if (newPath != imagePath) ImageStore.deleteFile(imagePath)
                imagePath = newPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建习惯" else "编辑习惯") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                Text("图标", style = MaterialTheme.typography.labelMedium)
                IconPicker(
                    selectedIconName = iconName,
                    onIconSelected = { iconName = it }
                )
                Text("颜色", style = MaterialTheme.typography.labelMedium)
                ColorPickerRow(
                    selected = color,
                    colors = colors,
                    onSelected = { color = it }
                )
                ImagePickRow(
                    imagePath = imagePath,
                    onPick = { imagePicker.launch("image/*") },
                    onRemove = { ImageStore.deleteFile(imagePath); imagePath = "" }
                )
                Text("每周目标：$target 次", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(name.trim(), iconName, color, target, imagePath)
                }
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

/**
 * 图片选择行：可选图片作为海报背景；已选时显示缩略图 + 更换/移除。
 * 供习惯与纪念日对话框共用。
 */
@Composable
fun ImagePickRow(
    imagePath: String,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "海报背景图（可选）",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.weight(1f)
        )
        if (imagePath.isNotBlank()) {
            val preview = remember(imagePath) { ImageStore.decodeScaled(imagePath) }?.asImageBitmap()
            if (preview != null) {
                Image(
                    bitmap = preview,
                    contentDescription = "背景图预览",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(6.dp))
            }
            TextButton(onClick = onPick) { Text("更换") }
            TextButton(onClick = onRemove) { Text("移除", color = MaterialTheme.colorScheme.error) }
        } else {
            TextButton(onClick = onPick) { Text("选择图片") }
        }
    }
}
