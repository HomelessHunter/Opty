package com.example.tasksapp.dailytodo.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.RoutineAdapter
import com.example.tasksapp.adapter.SwipeEraser
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.dailytodo.addTodoDialog.AddTodoDialog
import com.example.tasksapp.databinding.DailyRoutineFragmentBinding
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class Routine : Fragment() {

    private lateinit var binding: DailyRoutineFragmentBinding
    private val viewModel: DailyTasksViewModel by activityViewModels()

    private val formatterRoutine = SimpleDateFormat("d MMM", Locale.getDefault())

    companion object {
        const val ROUTINE_ID = "ROUTINE"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DailyRoutineFragmentBinding.inflate(inflater, container, false)

        val adapter = RoutineAdapter()
        val recyclerView = binding.routineRecyclerView
        recyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.emptyListCheckRoutine.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyRoutineText.text = getString(R.string.your_routine_list_is_empty)
                } else {
                    binding.emptyRoutineText.text = ""
                }
            }
        }

        lifecycleScope.launch {
            viewModel.todoList.collectLatest { pagingData ->
                pagingData.let {
                    adapter.submitData(pagingData)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.averageTodoTimeList.collectLatest {
                it?.let {
                    binding.averageTimeValue.text = setElapsedTime(it.last().time)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.averageTodoTimeList.collectLatest {
                it?.let {
                    val index = it.lastIndex - 1
                    if (index >= 0) {
                        val time = it[index]
                        binding.yesterdayText.text = formatterRoutine.format(time.finishDate)
                        binding.yesterdayTimeValue.text = setElapsedTime(time.time)
                    } else {
                        binding.yesterdayText.text = formatterRoutine.format(
                            Calendar.getInstance(Locale.getDefault()).timeInMillis - (24 * 3_600_000L))
                        binding.yesterdayTimeValue.text = setElapsedTime(0)
                    }
                }
            }
        }

        binding.addRoutine.setOnClickListener {
            val dialog = AddTodoDialog(viewModel, ROUTINE_ID)
            if (childFragmentManager.findFragmentByTag(AddTodoDialog.TAG) == null) {
                dialog.show(childFragmentManager, AddTodoDialog.TAG)
            }
        }
        binding.closeRoutine.setOnClickListener {
            findNavController().navigateUp()
        }

        val swipeEraser = object : SwipeEraser(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as RoutineAdapter.RoutineTaskViewHolder).todoDaily?.let {
                    viewModel.deleteTodo(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeEraser)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }
}