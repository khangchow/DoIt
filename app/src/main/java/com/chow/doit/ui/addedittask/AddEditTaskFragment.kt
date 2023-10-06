package com.chow.doit.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.chow.doit.R
import com.chow.doit.databinding.FragmentAddEditTaskBinding

class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            etTaskName.setText(viewModel.taskName)
            cbImportant.apply {
                isChecked = viewModel.taskImportance
                jumpDrawablesToCurrentState()
            }
            tvCreatedDate.apply {
                isVisible = viewModel.task != null
                text = "Created: ${viewModel.task?.formattedDate}"
            }
        }
    }
}