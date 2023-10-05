package com.chow.doit.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chow.doit.data.Task
import com.chow.doit.databinding.ItemTaskBinding

class TasksAdapter(
    private val onItemClicked: (Task) -> Unit,
    private val onCheckBoxChecked: (Task, Boolean) -> Unit
) : ListAdapter<Task, TasksAdapter.TasksViewHolder>(DiffCallback()) {

    inner class TasksViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        onItemClicked(task)
                    }
                }
                cbCompleted.apply {
                    setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val task = getItem(position)
                            onCheckBoxChecked(task, isChecked)
                        }
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                cbCompleted.isChecked = task.isCompleted
                tvName.apply {
                    text = task.name
                    paint.isStrikeThruText = task.isCompleted
                    ivPriority.isVisible = task.isImportant
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem

    }
}