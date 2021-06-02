package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.Task
import com.example.tasksapp.databinding.ListItemFinishedTaskBinding

class TaskAdapterFinished( private val restoreClickListener: RestoreClickListener,
                           private val statsClickListener: StatsClickListener)
    : ListAdapter<Task, TaskAdapterFinished.FinishedTaskListViewHolder>(DiffCallBack()) {

    class FinishedTaskListViewHolder private constructor(private val binding: ListItemFinishedTaskBinding)
        : RecyclerView.ViewHolder(binding.root) {

        var project: Task? = null

        fun bindFinished(task: Task, restoreClickListener: RestoreClickListener, statsClickListener: StatsClickListener) {
            project = task
            binding.task = task
            binding.restoreListener = restoreClickListener
            binding.statsListener = statsClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): FinishedTaskListViewHolder {
                val binding = ListItemFinishedTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return FinishedTaskListViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinishedTaskListViewHolder {
        return FinishedTaskListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: FinishedTaskListViewHolder, position: Int) {
        val task = getItem(position)
        holder.bindFinished(task, restoreClickListener, statsClickListener)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.taskId == newItem.taskId
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
class RestoreClickListener(val longListener: (id: Long) -> Unit) {
    fun onRestored(id: Long) = longListener(id)
}

class StatsClickListener(val clickListener: (id: Long) -> Unit) {
    fun onShowStats(id: Long) = clickListener(id)
}