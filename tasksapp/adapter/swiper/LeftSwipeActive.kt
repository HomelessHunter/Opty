package com.example.tasksapp.adapter.swiper

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.R

abstract class LeftSwipeActive(context: Context)
     : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
     private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_archive_black_24dp)
     private val intrinsicWidth = deleteIcon?.intrinsicWidth
     private val intrinsicHeight = deleteIcon?.intrinsicHeight
     private val background = ResourcesCompat.getDrawable(context.resources, R.drawable.swipe_archive_drawable, null)

     override fun onMove(
         recyclerView: RecyclerView,
         viewHolder: RecyclerView.ViewHolder,
         target: RecyclerView.ViewHolder
     ): Boolean {
         return false
     }

     override fun onChildDraw(
         c: Canvas,
         recyclerView: RecyclerView,
         viewHolder: RecyclerView.ViewHolder,
         dX: Float,
         dY: Float,
         actionState: Int,
         isCurrentlyActive: Boolean
     ) {
         val itemView = viewHolder.itemView
         val itemHeight = itemView.bottom - itemView.top

         background?.setBounds(itemView.left - dX.toInt(), itemView.top,
             itemView.right - 30, itemView.bottom)
         background?.draw(c)

         val deleteIconMargin = (itemHeight - intrinsicHeight!!) / 2
         val deleteIconTop = itemView.top + deleteIconMargin
         val deleteIconLeft = itemView.left + deleteIconMargin + intrinsicWidth!!
         val deleteIconRight = itemView.left + deleteIconMargin
         val deleteIconBottom = deleteIconTop + intrinsicHeight

         deleteIcon?.setBounds(deleteIconRight, deleteIconTop, deleteIconLeft, deleteIconBottom)
         deleteIcon?.draw(c)

         super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
     }
 }