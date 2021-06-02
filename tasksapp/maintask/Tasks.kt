package com.example.tasksapp.maintask

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
import com.example.tasksapp.adapter.*
import com.example.tasksapp.adapter.swiper.LeftSwipeActive
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.databinding.TasksFragmentBinding
import com.example.tasksapp.maintask.addMainTask.AddMainTaskDialog
import com.example.tasksapp.utilities.setElapsedTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class Tasks : Fragment() {

    private lateinit var factory: MainTaskFactory

    private val viewModel: MainTaskViewModel by activityViewModels { factory }
    private lateinit var binding: TasksFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = requireActivity().application
        val database = TaskDatabase.getInstance(application)
        factory = MainTaskFactory(database.taskDataDao, database.tagsDao, database.subtaskDataDao, database.todaySessionDao, application)
        lifecycleScope.launch {
            viewModel.getTagList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TasksFragmentBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        lifecycleScope.launch {
            viewModel.totalTime.collectLatest {
                binding.totalTimeValue.text = setElapsedTime(it)
            }
        }

        lifecycleScope.launch {
            viewModel.activeProjects.collectLatest {
                binding.activeProjectsCount.text = "$it"
            }
        }

        lifecycleScope.launch {
            viewModel.finishedProjects.collectLatest {
                binding.finishedProjectsCount.text = "$it"
            }
        }

        val adapter = TaskAdapter(TaskClickListener {
            viewModel.getTaskId(it)
        }, ShowOptionsDialog {
            viewModel.showDialog(it)
        })

        val adapterFinished = TaskAdapterFinished(RestoreClickListener {
            viewModel.setRestoreTask(it)
        }, StatsClickListener {
            viewModel.setShowStats(it)
        })

        val mainRecyclerView = binding.recycleViewActive
        mainRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        mainRecyclerView.layoutManager = layoutManager
        mainRecyclerView.setHasFixedSize(true)


        val finishedRecyclerView = binding.recycleViewFinished
        finishedRecyclerView.adapter = adapterFinished
        val layoutManagerFinished = LinearLayoutManager(activity)
        layoutManagerFinished.isSmoothScrollbarEnabled = true
        finishedRecyclerView.layoutManager = layoutManagerFinished
        mainRecyclerView.setHasFixedSize(true)

        val swipeActive = object : LeftSwipeActive(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as TaskAdapter.TaskListViewHolder).project?.let {
                    viewModel.updateTaskState(it.taskId)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeActive)
        itemTouchHelper.attachToRecyclerView(binding.recycleViewActive)

        val swipeEraser = object : SwipeEraser(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as TaskAdapterFinished.FinishedTaskListViewHolder).project?.let {
                    viewModel.deleteTask(it.taskId)
                    viewModel.deleteSubtasks(it.taskId)
                }
            }
        }
        val itemTouchHelperFinished = ItemTouchHelper(swipeEraser)
        itemTouchHelperFinished.attachToRecyclerView(finishedRecyclerView)


        lifecycleScope.launch {
            viewModel.tasks.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyBlob.visibility = View.VISIBLE
                    binding.noActiveProjects.visibility = View.VISIBLE
                } else {
                    binding.emptyBlob.visibility = View.GONE
                    binding.noActiveProjects.visibility = View.GONE
                }
                adapter.submitList(it)
            }
        }

        lifecycleScope.launch {
            viewModel.finishedTasks.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyBlobFinished.visibility = View.VISIBLE
                    binding.noFinishedProjets.visibility = View.VISIBLE
                } else {
                    binding.emptyBlobFinished.visibility = View.GONE
                    binding.noFinishedProjets.visibility = View.GONE
                }
                adapterFinished.submitList(it)
            }
        }

        viewModel.showStats.observe(viewLifecycleOwner, {
            it?.let {
                findNavController().navigate(TasksDirections.actionTasksToChartStatisticsFragment(it))
                viewModel.resetShowStats()
            }
        })

        viewModel.showOptionsDialog.observe(viewLifecycleOwner, { id ->
            id?.let {
                binding.finishButton.show()
                binding.dismissFabButton.show()
                finishProject(it)
                viewModel.resetShowDialog()
            }
        })

        viewModel.isTaskRestored.observe(viewLifecycleOwner, { id ->
            id?.let {
                binding.dismissFabButton.show()
                binding.deleteButton.show()
                binding.restoreButton.show()
                restoreOrDeleteProject(id)
                viewModel.resetRestoredTask()
            }
        })

        binding.dismissFabButton.setOnClickListener {
            binding.finishButton.hide()
            binding.restoreButton.hide()
            binding.deleteButton.hide()
            binding.mainTitle.text = getString(R.string.projects)
            binding.dismissFabButton.visibility = View.GONE
        }

        binding.fab.setOnClickListener {
            val addTaskDialog = AddMainTaskDialog(viewModel)
            if (childFragmentManager.findFragmentByTag(AddMainTaskDialog.TAG) == null) {
                addTaskDialog.show(childFragmentManager, AddMainTaskDialog.TAG)
            }
        }

        binding.importantMainSwitch.setOnCheckedChangeListener { _, b ->
            lifecycleScope.launch {
                if (b) {
                    adapter.submitList(viewModel.getImportantProjects())
                } else {
                    viewModel.tasks.collectLatest {
                        adapter.submitList(it)
                    }
                }
            }
        }

        viewModel.taskId.observe(viewLifecycleOwner, {
            it?.let {
                findNavController().navigate(TasksDirections.actionTasksToTaskDescription(it))
                viewModel.onShipTaskId()
            }
        })

        binding.todoMainButton.setOnClickListener {
            findNavController().navigate(TasksDirections.actionTasksToDailyTasks())
        }
        binding.timerMainButton.setOnClickListener {
            findNavController().navigate(TasksDirections.actionTasksToTimer())
        }
        binding.tagsMainButton.setOnClickListener {
            findNavController().navigate(TasksDirections.actionTasksToBookmarksFragment())
        }

        binding.settingsMainButton.setOnClickListener {
            findNavController().navigate(TasksDirections.actionTasksToSettingsFragment())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.seconMainBackground, resources)
    }

    private fun finishProject(idTask: Long) {
       lifecycleScope.launch {
           val s = viewModel.database.getTaskWithKey(idTask)
           binding.mainTitle.text = s.taskTag
           binding.finishButton.setOnClickListener {
               viewModel.updateTaskState(idTask)
               binding.finishButton.hide()
               binding.dismissFabButton.hide()
               binding.mainTitle.text = getString(R.string.projects)
           }
       }
    }
    private fun restoreOrDeleteProject(idTask: Long) {
        lifecycleScope.launch {
            val s = viewModel.database.getTaskWithKey(idTask)
            binding.mainTitle.text = s.taskTag
            binding.restoreButton.setOnClickListener {
                viewModel.updateTaskState(idTask)
                binding.restoreButton.hide()
                binding.dismissFabButton.hide()
                binding.deleteButton.hide()
                binding.mainTitle.text = getString(R.string.projects)
            }
            binding.deleteButton.setOnClickListener {
                viewModel.deleteTask(idTask)
                viewModel.deleteSubtasks(idTask)
                binding.deleteButton.hide()
                binding.restoreButton.hide()
                binding.dismissFabButton.hide()
                binding.mainTitle.text = getString(R.string.projects)
            }
        }
    }
}
