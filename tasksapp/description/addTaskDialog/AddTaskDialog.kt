package com.example.tasksapp.description.addTaskDialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.AddTaskDialogBinding
import com.example.tasksapp.description.TaskDescriptionViewModel
import com.example.tasksapp.utilities.isTextCorrect
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class AddTaskDialog(private val viewModel: TaskDescriptionViewModel) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "Add_Task_Dialog"
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
        binding.editText.requestFocus()

        binding.acceptButton.setOnClickListener {
            val textField = binding.textFieldAdd.editText!!.text
            if (!isTextCorrect(textField)) {
                binding.textFieldAdd.error = "Error"
            } else {
                binding.textFieldAdd.error = null
                viewModel.onGetSubtaskText(textField.toString())
                viewModel.onInsertSubtask()
                binding.textFieldAdd.editText!!.text = null

                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.textFieldAdd.windowToken, 0)
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
}