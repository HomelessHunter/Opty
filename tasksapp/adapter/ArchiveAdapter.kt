package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.DailyTodoSchedule
import com.example.tasksapp.database.ScheduleWithTodo
import com.example.tasksapp.databinding.ListItemArchiveBinding

class ArchiveAdapter(private val archiveListener: ArchiveListener, private val deleteArchiveListener: DeleteArchiveListener) : PagingDataAdapter<ScheduleWithTodo, ArchiveAdapter.ArchiveViewHolder>(
    DIFF_CALLBACK) {

    class ArchiveViewHolder private constructor(private val binding: ListItemArchiveBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(scheduleWithTodo: ScheduleWithTodo?, archiveListener: ArchiveListener, deleteArchiveListener: DeleteArchiveListener) {
            binding.scheduleVar = scheduleWithTodo
            binding.listener = archiveListener
            binding.deleteListener = deleteArchiveListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ArchiveViewHolder {
                val binding = ListItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return ArchiveViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ArchiveViewHolder, position: Int) {
        val scheduleWithTodo = getItem(position)
        holder.bind(scheduleWithTodo, archiveListener, deleteArchiveListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveViewHolder{
        return ArchiveViewHolder.from(parent)
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ScheduleWithTodo>() {
            override fun areItemsTheSame(oldItem: ScheduleWithTodo, newItem: ScheduleWithTodo): Boolean {
                return oldItem.schedule.todoScheduleId == newItem.schedule.todoScheduleId
            }

            override fun areContentsTheSame(oldItem: ScheduleWithTodo, newItem: ScheduleWithTodo): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class ArchiveListener(val listener: (showInfo: String) -> Unit) {
    fun showInfo(value: String) = listener(value)
}

class DeleteArchiveListener(val deleteListener: (deleteArchive: DailyTodoSchedule) -> Unit) {
    fun deleteArchive(value: DailyTodoSchedule) = deleteListener(value)
}