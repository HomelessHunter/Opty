package com.example.tasksapp.statistics

import android.os.Bundle
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.ChartAdapter
import com.example.tasksapp.database.TaskDatabase
import com.example.tasksapp.database.TodaySessionResult
import com.example.tasksapp.databinding.ChartFragmentBinding
import com.example.tasksapp.utilities.setElapsedTime
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@ExperimentalCoroutinesApi
class ChartStatisticsFragment : Fragment() {

    private lateinit var factory: ChartStatisticsFactory
    private val viewModel: ChartStatisticsViewModel by viewModels { factory }
    private val args: ChartStatisticsFragmentArgs by navArgs()
    private lateinit var binding: ChartFragmentBinding
    private val markFormatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = requireActivity().application
        val database = TaskDatabase.getInstance(app)
        factory = ChartStatisticsFactory(args.parentTaskId, database.matrixResultDao, database.todaySessionDao, database.taskDataDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChartFragmentBinding.inflate(inflater, container, false)

        val pieChart = binding.pieChart
        val lineChart = binding.lineChart

        lifecycleScope.launch {
            viewModel.mainTask.collectLatest {
                binding.chartToolbar.subtitle = it.taskTag
            }
        }

        binding.chartToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val pieFormatter = object : ValueFormatter() {
            private val format = DecimalFormat("###,##0.0")
            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return format.format(value).plus(" %")
            }
        }

        val lineYFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return setElapsedTime(value.toLong())
            }
        }

        lifecycleScope.launch {
            viewModel.xZoom.collectLatest {
                if (it > 0f) {
                    binding.zoomInButton.visibility = View.VISIBLE
                    binding.zoomOutButton.visibility = View.VISIBLE
                }
            }
        }

        val marker = object : MarkerView(context, R.layout.custom_marker) {
            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                binding.lineChartDescription.text = markFormatter.format(viewModel.lineChartDateResultList.value[e?.x!!.toInt()])
                binding.lineChartValue.text = setElapsedTime(e.y.toLong())
                viewModel.xZoom.value = e.x
                viewModel.yZoom.value = e.y
                super.refreshContent(e, highlight)
            }
        }

        binding.zoomInButton.setOnClickListener {
            binding.lineChart.zoom(2f, 2f, viewModel.xZoom.value, viewModel.yZoom.value, YAxis.AxisDependency.RIGHT)
        }

        binding.zoomOutButton.setOnClickListener {
            binding.lineChart.zoomOut()
        }

        lifecycleScope.launch {
            viewModel.pieChartResultList.collectLatest { chartResult ->
                val pieChartLegend = pieChart.legend

                pieChart.isDrawHoleEnabled = true
                pieChart.setHoleColor(resources.getColor(R.color.seconMainBackground, null))
                pieChart.setDrawRoundedSlices(true)
                pieChart.description.isEnabled = false
                pieChartLegend.isEnabled = false
                pieChart.setDrawSlicesUnderHole(true)
                pieChart.setDrawEntryLabels(false)

                val entries = chartResult?.map { PieEntry(it.finishTime, it.name) }
                val colorsList = mutableListOf<Int>()
                chartResult?.map {
                    when(it.idMatrix) {
                        "${args.parentTaskId}1" -> colorsList += resources.getColor(R.color.bookmark1, null)

                        "${args.parentTaskId}2" -> colorsList += resources.getColor(R.color.bookmark3, null)

                        "${args.parentTaskId}3" -> colorsList += resources.getColor(R.color.bookmark2, null)

                        "${args.parentTaskId}4" -> colorsList += resources.getColor(R.color.bookmark4, null)
                    }
                }

                val set = PieDataSet(entries, "")
                set.colors = colorsList
                set.valueTextSize = 16f
                val data = PieData(set)
                data.setValueTextColor(resources.getColor(R.color.seconMainBackground, null))
                data.setValueFormatter(pieFormatter)
                pieChart.data = data
                pieChart.invalidate()
            }
        }

        lifecycleScope.launch {
            viewModel.matrixResultList.collectLatest { list ->
                list.map {
                    when(it.taskMatrixResultId) {
                        "${args.parentTaskId}1" -> binding.ImpUrgTextView.text = getString(R.string.important_urgent_pie, setElapsedTime(it.finishMatrixTime))

                        "${args.parentTaskId}2" -> binding.ImpNotUrgTextView.text = getString(R.string.important_not_urgent_pie, setElapsedTime(it.finishMatrixTime))

                        "${args.parentTaskId}3" -> binding.notImpUrgTextView.text = getString(R.string.not_important_urgent_pie, setElapsedTime(it.finishMatrixTime))

                        "${args.parentTaskId}4" -> binding.notImpNotUrgTextView.text = getString(R.string.not_important_not_urgent_pie, setElapsedTime(it.finishMatrixTime))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.lineChartResultList.collectLatest { list ->
                lineChart.description.isEnabled = false
                lineChart.legend.isEnabled = false
                lineChart.axisLeft.isEnabled = false
                lineChart.axisRight.valueFormatter = lineYFormatter
                lineChart.marker = marker
                lineChart.xAxis.setDrawGridLines(false)
                lineChart.xAxis.isEnabled = false
                lineChart.axisRight.isEnabled = false
                lineChart.axisRight.setDrawGridLines(false)
                lineChart.isDoubleTapToZoomEnabled = true

                val entries = list?.map { Entry(it.sessionIndex.toFloat(), it.duration) }
                val set = LineDataSet(entries, "BarDataSet")
                set.axisDependency = YAxis.AxisDependency.RIGHT
                set.color = resources.getColor(R.color.secondaryColor, null)
                set.setDrawValues(false)
                set.setDrawFilled(true)
                set.fillDrawable = ResourcesCompat.getDrawable(resources, R.drawable.chart_gradient, null)
                set.setCircleColor(resources.getColor(R.color.primaryDarkColor, null))
                set.circleHoleColor = resources.getColor(R.color.secondaryColor, null)
                set.circleRadius = 3f
                set.circleHoleRadius = 1.5f
                val data = LineData(set)
                lineChart.data = data
                lineChart.invalidate()
            }
        }

        val adapter = ChartAdapter()
        val recyclerView = binding.chartRecyclerView
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.isSmoothScrollbarEnabled = true
        recyclerView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewModel.todaySessionList.collectLatest {
                var list: List<TodaySessionResult> = listOf()
                withContext(Dispatchers.Default) {
                    list = it.filter { it.sessionDate > 0L }
                }
                if (list.isNullOrEmpty()) {
                    binding.pieChartButton.visibility = View.GONE
                    binding.emptyChartBlob.visibility = View.VISIBLE
                    binding.emptyChartText.visibility = View.VISIBLE
                } else {
                    binding.pieChartButton.visibility = View.VISIBLE
                    binding.emptyChartBlob.visibility = View.GONE
                    binding.emptyChartText.visibility = View.GONE
                }
                adapter.submitList(list)
            }
        }

        binding.pieChartButton.setOnClickListener {
            setPieChart()
        }

        binding.lineChartButton.setOnClickListener {
            setLineChart()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }

    private fun setPieChart() {
        binding.lineChartDescription.visibility = View.GONE
        binding.lineChartValue.visibility = View.GONE
        binding.lineChart.visibility = View.GONE
        binding.chartRecyclerView.visibility = View.GONE
        binding.pieChartButton.visibility = View.GONE
        binding.zoomInButton.visibility = View.GONE
        binding.zoomOutButton.visibility = View.GONE
        binding.lineChartButton.visibility = View.VISIBLE
        binding.pieChart.visibility = View.VISIBLE

        binding.ImpUrgTextView.visibility = View.VISIBLE
        binding.ImpNotUrgTextView.visibility = View.VISIBLE
        binding.notImpUrgTextView.visibility = View.VISIBLE
        binding.notImpNotUrgTextView.visibility = View.VISIBLE
    }

    private fun setLineChart() {
        binding.pieChart.visibility = View.GONE
        binding.lineChartButton.visibility = View.GONE

        binding.ImpUrgTextView.visibility = View.GONE
        binding.ImpNotUrgTextView.visibility = View.GONE
        binding.notImpUrgTextView.visibility = View.GONE
        binding.notImpNotUrgTextView.visibility = View.GONE

        binding.lineChart.visibility = View.VISIBLE
        binding.pieChartButton.visibility = View.VISIBLE
        binding.lineChartDescription.visibility = View.VISIBLE
        binding.lineChartValue.visibility = View.VISIBLE
        binding.chartRecyclerView.visibility = View.VISIBLE
    }
}