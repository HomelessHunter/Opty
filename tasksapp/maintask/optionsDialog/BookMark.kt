package com.example.tasksapp.maintask.optionsDialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.databinding.BookmarkLabelsBottomDialogBinding
import com.example.tasksapp.maintask.MainTaskViewModel
import com.example.tasksapp.utilities.isTextCorrect
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
class BookMark(private val viewModel: MainTaskViewModel) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "BOOKMARK"
    }

    private lateinit var binding: BookmarkLabelsBottomDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheet)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BookmarkLabelsBottomDialogBinding.inflate(inflater, container, false)
        binding.textFieldBookmark.editText?.requestFocus()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.acceptButton.setOnClickListener {
            val textField = binding.textFieldBookmark.editText!!.text
            if (!isTextCorrect(textField)) {
                binding.textFieldBookmark.error = "Error"
            } else {
                binding.textFieldBookmark.error = null
                viewModel.getTagsText(textField.toString())
                viewModel.insertTag()
                binding.textFieldBookmark.editText!!.text = null

                val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.textFieldBookmark.windowToken, 0)
            }
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.radioGroupBookmark.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                binding.colorPicker1.id -> setTagsColor(1)
                binding.colorPicker2.id -> setTagsColor(2)
                binding.colorPicker3.id -> setTagsColor(3)
                binding.colorPicker4.id -> setTagsColor(4)
                binding.colorPicker5.id -> setTagsColor(5)
                binding.colorPicker6.id -> setTagsColor(6)
                binding.colorPicker7.id -> setTagsColor(7)
                binding.colorPicker8.id -> setTagsColor(8)
            }
        }
    }

    private fun setTagsColor(value: Int) {
        viewModel.getTagsColor(value)
    }
}