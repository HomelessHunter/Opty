package com.example.tasksapp.maintask.bookmarksFragment.tagInfo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.databinding.TagInfoFragmentBinding
import com.example.tasksapp.utilities.setElapsedTime
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
class TagInfoFragment : Fragment() {

    private lateinit var factory: TagInfoFactory
    private val viewModel: TagInfoViewModel by viewModels { factory }
    private val args: TagInfoFragmentArgs by navArgs()
    private lateinit var binding: TagInfoFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = TaskDatabase.getInstance(requireContext())
        factory = TagInfoFactory(database.taskDataDao, args.tag, database.todaySessionDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TagInfoFragmentBinding.inflate(inflater, container, false)

        val pieChart = binding.tagPieChart

        val pieFormatter = object : ValueFormatter() {
            private val format = DecimalFormat("###,##0.0")
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return format.format(value).plus("%")
            }
        }

        binding.tagInfoBar.title = args.tag

        binding.tagInfoBar.setNavigationOnClickListener {
            findNavController().navigateUp()
            binding.tagInfoBar.setNavigationIconColor(ResourcesCompat.getColor(resources, R.color.secondTextColor, null))
        }

        lifecycleScope.launch {
            viewModel.totalTime.collectLatest {
                binding.tagTotalTimeValue.text = setElapsedTime(it)
            }
        }

        lifecycleScope.launch {
            viewModel.longestProject.collectLatest {
                binding.logestProjectValue.text = setElapsedTime(it)
            }
        }

        lifecycleScope.launch {
            viewModel.shortestProject.collectLatest {
                binding.shortestProjectValue.text = setElapsedTime(it)
            }
        }

        lifecycleScope.launch {
            viewModel.tasksPerHour.collectLatest {
                binding.tasksPerHourValue.text = "${it.roundToInt()}"
            }
        }

        lifecycleScope.launch {
            viewModel.timeForOneTask.collectLatest {
                binding.timeForOneTaskValue.text = setElapsedTime(it)
            }
        }

        val marker = object : MarkerView(context, R.layout.custom_marker) {
            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                val s = e as PieEntry
                binding.tagChartLabel.text = s.label
                viewModel.setMinMaxInfo(s.label)
                viewModel.setTasksPerHour(s.label)
                viewModel.setTotalTime(s.label)
                super.refreshContent(e, highlight)
            }
        }

        lifecycleScope.launch {
            viewModel.tagPieChartList.collectLatest { pieChartList ->
                pieChart.isDrawHoleEnabled = true
                pieChart.setHoleColor(resources.getColor(R.color.seconMainBackground, null))
                pieChart.description.isEnabled = false
                pieChart.legend.isEnabled = false
                pieChart.setDrawEntryLabels(false)
                pieChart.setDrawSlicesUnderHole(true)
                pieChart.marker = marker
                pieChart.setTransparentCircleAlpha(0)
                pieChart.holeRadius = 80f

                val entries = pieChartList.map { PieEntry(it.finishTimer, it.name) }
                val colorList = listOf(
                    resources.getColor(R.color.tagColor1, null),
                    resources.getColor(R.color.tagColor2, null),
                    resources.getColor(R.color.tagColor3, null),
                    resources.getColor(R.color.tagColor4, null),
                    resources.getColor(R.color.tagColor5, null),
                    resources.getColor(R.color.tagColor6, null),
                    resources.getColor(R.color.tagColor7, null),
                    resources.getColor(R.color.tagColor8, null),
                    resources.getColor(R.color.tagColor9, null),
                    resources.getColor(R.color.tagColor10, null))

                val set = PieDataSet(entries, "")
                set.colors = colorList
                set.valueTextSize = 16f
                val data = PieData(set)
                data.setValueTextColor(resources.getColor(R.color.primaryTextColor, null))
                data.setValueFormatter(pieFormatter)
                pieChart.data = data
                pieChart.invalidate()
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }
}