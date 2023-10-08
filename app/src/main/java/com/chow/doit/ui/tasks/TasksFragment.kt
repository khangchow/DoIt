package com.chow.doit.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chow.doit.R
import com.chow.doit.data.SortOrder
import com.chow.doit.databinding.FragmentTasksBinding
import com.chow.doit.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val tasksAdapter = TasksAdapter(
            onItemClicked = { task ->
                viewModel.onTaskSelected(task)
            },
            onCheckBoxChecked = { task, isChecked ->
                viewModel.onTaskCheckChanged(task, isChecked)
            }
        )
        val menuHost: MenuHost = requireActivity()
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_tasks, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.apply {
                    if (viewModel.searchQuery.value.isNullOrBlank().not()) {
                        searchItem.expandActionView()
                        setQuery(viewModel.searchQuery.value, false)
                    }
                    onQueryTextChanged {
                        viewModel.searchQuery.value = it
                    }
                    lifecycleScope.launch {
                        menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                            viewModel.preferencesFlow.first().hideCompleted
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort_by_name -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                        true
                    }
                    R.id.action_sort_by_created_date -> {
                        viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                        true
                    }
                    R.id.action_hide_completed_tasks -> {
                        menuItem.isChecked = !menuItem.isChecked
                        viewModel.onHideCompleted(menuItem.isChecked)
                        true
                    }
                    R.id.action_delete_completed_tasks -> {

                        true
                    }
                    else -> false
                }
            }
        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        binding.apply {
            rvTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                ItemTouchHelper(object :
                    ItemTouchHelper.SimpleCallback(
                        0,
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    ) {
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ) = false

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                        viewModel.onTaskSwiped(task)
                    }

                }).attachToRecyclerView(this)
            }
            fabAddTask.setOnClickListener {
                viewModel.onAddNewTaskClicked()
            }
        }
        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }
        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                            Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onUndoDeleteClicked(event.task)
                                }
                                .show()
                        }
                        is TasksViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                            val action =
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment("New Task")
                            findNavController().navigate(action)
                        }
                        is TasksViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                            val action =
                                TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment("Edit Task")
                            action.task = event.task
                            findNavController().navigate(action)
                        }
                        is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> Snackbar.make(
                            requireView(),
                            "Task deleted",
                            Snackbar.LENGTH_LONG
                        )
                            .setText(event.msg)
                            .show()
                    }
                }
            }
        }
    }
}