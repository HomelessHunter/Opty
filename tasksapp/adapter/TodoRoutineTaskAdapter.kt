package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ListItemDailytaskRoutineBinding

class TodoRoutineTaskAdapter(private val completionListener: TodoRoutineCompletionListener) : PagingDataAdapter<DailyTodoTask, TodoRoutineTaskAdapter.TodoRoutineTaskViewHolder>(
    DIFF_CALLBACK) {

    class TodoRoutineTaskViewHolder private constructor(private val binding: ListItemDailytaskRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        var todo: DailyTodoTask? = null

        fun bind(todoTask: DailyTodoTask?, completionListener: TodoRoutineCompletionListener) {
            todo = todoTask
            binding.todoTask = todoTask
            binding.completionListener = completionListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): TodoRoutineTaskViewHolder {
                val binding = ListItemDailytaskRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return TodoRoutineTaskViewHolder(binding)
            }
        }
    }


    override fun onBindViewHolder(holder: TodoRoutineTaskViewHolder, position: Int) {
        val todoItem = getItem(position)
        holder.bind(todoItem, completionListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoRoutineTaskViewHolder {
        return TodoRoutineTaskViewHolder.from(parent)
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


class TodoRoutineCompletionListener(val listener: (setCompletion: Long) -> Unit) {
    fun setCompletion(value: Long) = listener(value)
}