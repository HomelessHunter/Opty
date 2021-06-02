package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ListItemDailytaskBinding


class TodoTaskAdapter(private val completionListener: TodoCompletionListener) : PagingDataAdapter<DailyTodoTask, TodoTaskAdapter.TodoTaskViewHolder>(
    DIFF_CALLBACK) {

    class TodoTaskViewHolder private constructor(private val binding: ListItemDailytaskBinding) : RecyclerView.ViewHolder(binding.root) {
        var todo: DailyTodoTask? = null

        fun bind(todoTask: DailyTodoTask?, completionListener: TodoCompletionListener) {
            todo = todoTask
            binding.todoTask = todoTask
            binding.completionListener = completionListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TodoTaskViewHolder {
                val binding = ListItemDailytaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TodoTaskViewHolder(binding)
            }
        }
    }


    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {
        val todoItem = getItem(position)
        holder.bind(todoItem, completionListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        return TodoTaskViewHolder.from(parent)
    }


    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<DailyTodoTask>() {
            override fun areItemsTheSame(oldItem: DailyTodoTask, newItem: DailyTodoTask): Boolean {
                return oldItem.todoTaskId == newItem.todoTaskId
            }

            override fun areContentsTheSame(oldItem: DailyTodoTask, newItem: DailyTodoTask): Boolean {
                return oldItem == newItem
            }
        }
    }
}


class TodoCompletionListener(val listener: (setCompletion: Long) -> Unit) {
    fun setCompletion(value: Long) = listener(value)
}
