package com.chow.doit.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.chow.doit.R
import com.chow.doit.databinding.FragmentAddEditTaskBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            etTaskName.apply {
                setText(viewModel.taskName)
                addTextChangedListener {
                    viewModel.taskName = it.toString()
                }
            }
            cbImportant.apply {
                isChecked = viewModel.taskImportance
                jumpDrawablesToCurrentState()
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.taskImportance = isChecked
                }
            }
            tvCreatedDate.apply {
                isVisible = viewModel.task != null
                text = "Created: ${viewModel.task?.formattedDate}"
            }
            fabSaveTask.setOnClickListener {
                viewModel.onSaveClicked()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                            binding.etTaskName.clearFocus()
                            setFragmentResult(
                                "add_edit_request",
                                bundleOf("add_edit_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}