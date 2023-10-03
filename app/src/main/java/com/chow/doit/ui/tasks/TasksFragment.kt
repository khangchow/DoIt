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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.chow.doit.R
import com.chow.doit.databinding.FragmentTasksBinding
import com.chow.doit.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks) {
    private val viewModel: TasksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTasksBinding.bind(view)
        val tasksAdapter = TasksAdapter()
        val menuHost: MenuHost = requireActivity()
        binding.apply {
            rvTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }
        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_fragment_tasks, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.onQueryTextChanged {
                    viewModel.searchQuery.value = it
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort_by_name -> {

                        true
                    }
                    R.id.action_sort_by_created_date -> {

                        true
                    }
                    R.id.action_hide_completed_tasks -> {
                        menuItem.isChecked = !menuItem.isChecked
                        true
                    }
                    R.id.action_delete_completed_tasks -> {

                        true
                    }
                    else -> false
                }
            }

        })
    }
}