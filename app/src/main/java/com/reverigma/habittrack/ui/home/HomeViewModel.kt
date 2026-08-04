package com.reverigma.habittrack.ui.home

import androidx.lifecycle.ViewModel
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.data.repo.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HabitRepository
) : ViewModel() {
    val habits: List<Habit> = repo.sampleHabits()
}
