package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.R
import kotlinx.android.synthetic.main.todo_schedule_header.view.*

data class ScheduleHeader(
    val title: String = ""
)

class ScheduleHeaderAdapter(private val scheduleHeader: ScheduleHeader) : RecyclerView.Adapter<ScheduleHeaderAdapter.ScheduleHeaderViewHolder>() {

    class ScheduleHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(scheduleHeader: ScheduleHeader) {
            itemView.scheduleHeader.text = scheduleHeader.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ScheduleHeaderViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.todo_schedule_header, parent, false)
    )

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ScheduleHeaderViewHolder, position: Int) {
        holder.bind(scheduleHeader)
    }
}