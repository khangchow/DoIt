package com.chow.doit.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.chow.doit.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {
    val searchQuery = MutableStateFlow("")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }
    val tasks = tasksFlow.asLiveData()
}