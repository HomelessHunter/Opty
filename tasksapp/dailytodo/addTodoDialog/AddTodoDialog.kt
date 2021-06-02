package com.example.tasksapp.dailytodo.addTodoDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tasksapp.R
import com.example.tasksapp.dailytodo.DailyTasksViewModel
import com.example.tasksapp.databinding.AddTaskDialogBinding
import com.example.tasksapp.utilities.isTextCorrect
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddTodoDialog(private val viewModel: DailyTasksViewModel, private val scheduleId: String) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ADD_TODO_DIALOG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = AddTaskDialogBinding.inflate(inflater, container, false)
        binding.textFieldAdd.hint = getString(R.string.todo)
        binding.editText.requestFocus()

        binding.acceptButton.setOnClickListener {
            val textField = binding.textFieldAdd.editText!!.text
            if (!isTextCorrect(textField)) {
                binding.textFieldAdd.error = "Error"
            } else {
                binding.textFieldAdd.error = null
                viewModel.onInsertTodoTask(scheduleId, textField.toString())
                binding.textFieldAdd.editText!!.text = null

            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }
}