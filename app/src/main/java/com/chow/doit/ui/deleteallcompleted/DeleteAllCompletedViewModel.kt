package com.chow.doit.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import com.chow.doit.data.TaskDao
import com.chow.doit.di.ApplicationScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun onConfirmClicked() = applicationScope.launch(Dispatchers.IO) {
        taskDao.deleteCompletedTasks()
    }
}