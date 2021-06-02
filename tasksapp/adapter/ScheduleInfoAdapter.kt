package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.DailyTodoTask
import com.example.tasksapp.databinding.ListScheduleInfoBinding

class ScheduleInfoAdapter : PagingDataAdapter<DailyTodoTask, ScheduleInfoAdapter.InfoTodoTaskViewHolder>(
    DIFF_CALLBACK) {

    class InfoTodoTaskViewHolder private constructor(private val binding: ListScheduleInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        var todoDaily: DailyTodoTask? = null

        fun bind(todo: DailyTodoTask?) {
            todoDaily = todo
            binding.todo = todo
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): InfoTodoTaskViewHolder {
                val binding = ListScheduleInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return InfoTodoTaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: InfoTodoTaskViewHolder, position: Int) {
        val dailyTodoTask = getItem(position)
        holder.bind(dailyTodoTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoTodoTaskViewHolder{
        return InfoTodoTaskViewHolder.from(parent)
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