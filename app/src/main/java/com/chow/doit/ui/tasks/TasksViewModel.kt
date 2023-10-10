package com.chow.doit.ui.tasks

import androidx.lifecycle.*
import com.chow.doit.data.PreferencesManager
import com.chow.doit.data.SortOrder
import com.chow.doit.data.Task
import com.chow.doit.data.TaskDao
import com.chow.doit.ui.ADD_TASK_RESULT_OK
import com.chow.doit.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
) : ViewModel() {
    private val _searchQuery = state.getLiveData("searchQuery", "")
    val searchQuery: LiveData<String> = _searchQuery
    val preferencesFlow = preferencesManager.preferencesFlow
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }
    val tasks = tasksFlow.asLiveData()

    fun onQueryTextChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch(Dispatchers.IO) {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompleted(hideCompleted: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckChanged(task: Task, isChecked: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        taskDao.update(task.copy(isCompleted = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        taskDao.insert(task)
    }

    fun onAddNewTaskClicked() = viewModelScope.launch(Dispatchers.IO) {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) = viewModelScope.launch(Dispatchers.IO) {
        when (result) {
            ADD_TASK_RESULT_OK -> {
                showTaskSavedConfirmationMessage("Task added")
            }
            EDIT_TASK_RESULT_OK -> {
                showTaskSavedConfirmationMessage("Task updated")
            }
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) =
        viewModelScope.launch(Dispatchers.IO) {
            tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
        }

    fun onDeleteAllCompletedTasksClicked() = viewModelScope.launch(Dispatchers.IO) {
        tasksEventChannel.send(TasksEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TasksEvent()
        object NavigateToAddTaskScreen : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        object NavigateToDeleteAllCompletedScreen : TasksEvent()
    }
}