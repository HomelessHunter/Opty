package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ListItemRoutineBinding

class RoutineAdapter : PagingDataAdapter<DailyTodoTask, RoutineAdapter.RoutineTaskViewHolder>(
    DIFF_CALLBACK) {

    class RoutineTaskViewHolder private constructor(private val binding: ListItemRoutineBinding) : RecyclerView.ViewHolder(binding.root) {
        var todoDaily: DailyTodoTask? = null

        fun bind(todo: DailyTodoTask?) {
            todoDaily = todo
            binding.routine = todo
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RoutineTaskViewHolder {
                val binding = ListItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return RoutineTaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RoutineTaskViewHolder, position: Int) {
        val routineTask = getItem(position)
        holder.bind(routineTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineTaskViewHolder{
        return RoutineTaskViewHolder.from(parent)
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