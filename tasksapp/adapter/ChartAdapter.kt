package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.database.TodaySessionResult
import com.example.tasksapp.databinding.ListItemChartBinding

class ChartAdapter : ListAdapter<TodaySessionResult, ChartAdapter.ChartViewHolder>(DIFF_CALLBACK) {

    class ChartViewHolder(private val binding: ListItemChartBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todaySession: TodaySessionResult) {
            binding.todaySession = todaySession
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ChartViewHolder {
                val binding = ListItemChartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ChartViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
        return ChartViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
        val todaySession = getItem(position)
        holder.bind(todaySession)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TodaySessionResult>() {
            override fun areItemsTheSame(
                oldItem: TodaySessionResult,
                newItem: TodaySessionResult
            ): Boolean {
                return oldItem.todaySessionId == newItem.todaySessionId
            }

            override fun areContentsTheSame(
                oldItem: TodaySessionResult,
                newItem: TodaySessionResult
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

}