package com.reverigma.habittrack.ui.anniversary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.habittrack.data.model.Anniversary
import com.reverigma.habittrack.data.model.REPEAT_MONTHLY
import com.reverigma.habittrack.data.model.REPEAT_NONE
import com.reverigma.habittrack.data.model.REPEAT_YEARLY
import com.reverigma.habittrack.data.repo.AnniversaryRepository
import com.reverigma.habittrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AnniversaryItem(
    val anniversary: Anniversary,
    val countdownDays: Long   // >=0 还有N天；<0 已过N天
)

data class AnniversaryUiState(
    val items: List<AnniversaryItem> = emptyList()
)

@HiltViewModel
class AnniversaryViewModel @Inject constructor(
    private val repo: AnniversaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnniversaryUiState())
    val uiState: StateFlow<AnniversaryUiState> = _uiState

    init { refresh() }

    private fun refresh() {
        viewModelScope.launch {
            val list = repo.anniversaries().first()
            val today = DateUtils.todayStr()
            val items = list.map { a ->
                AnniversaryItem(a, DateUtils.countdownDays(a.date, a.repeatType, today))
            }.sortedBy { it.countdownDays }
            _uiState.value = AnniversaryUiState(items)
        }
    }

    fun add(name: String, emoji: String, date: String, repeatType: String, note: String) {
        viewModelScope.launch {
            repo.add(
                Anniversary(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = emoji,
                    date = date,
                    repeatType = repeatType,
                    note = note
                )
            )
            refresh()
        }
    }

    fun delete(a: Anniversary) {
        viewModelScope.launch {
            repo.delete(a)
            refresh()
        }
    }
}

fun repeatLabel(r: String): String = when (r) {
    REPEAT_YEARLY -> "每年"
    REPEAT_MONTHLY -> "每月"
    REPEAT_NONE -> "不重复"
    else -> "不重复"
}
