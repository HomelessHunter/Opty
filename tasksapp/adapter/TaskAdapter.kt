package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.Task
import com.example.tasksapp.databinding.ListItemTaskBinding

class TaskAdapter(private val clickListener: TaskClickListener,
                  private val showOptionsDialog: ShowOptionsDialog)
    : ListAdapter<Task, TaskAdapter.TaskListViewHolder>(DiffCallBack()) {


    class TaskListViewHolder private constructor(private val binding: ListItemTaskBinding)
        : RecyclerView.ViewHolder(binding.root) {

        var project: Task? = null

        fun bind(clickListener: TaskClickListener, task: Task, showOptionsDialog: ShowOptionsDialog) {
            project = task
            binding.task = task
            binding.taskListener = clickListener
            binding.showDialogListener = showOptionsDialog
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TaskListViewHolder {
                val binding = ListItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TaskListViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListViewHolder {
        return TaskListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: TaskListViewHolder, position: Int) {
        val taskItem = getItem(position)
        holder.bind(clickListener, taskItem, showOptionsDialog)
    }

}

class DiffCallBack : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
       return oldItem.taskId == newItem.taskId
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
       return oldItem == newItem
    }
}

class TaskClickListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(id: Long) = clickListener(id)
}

class ShowOptionsDialog(val longListener: (id: Long) -> Unit) {
    fun onShowDialog(id: Long) = longListener(id)
}
