package com.example.tasksapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.R
import kotlinx.android.synthetic.main.todo_routine_header.view.*

data class RoutineHeader(
    val title: String = ""
)

class RoutineHeaderAdapter(private val routineHeader: RoutineHeader) : RecyclerView.Adapter<RoutineHeaderAdapter.RoutineHeaderViewHolder>() {

    class RoutineHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(header: RoutineHeader) {
            itemView.headerRoutine.text = header.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RoutineHeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.todo_routine_header, parent, false)
        )

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: RoutineHeaderViewHolder, position: Int) {
        holder.bind(routineHeader)
    }

}