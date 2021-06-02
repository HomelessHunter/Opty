package com.example.tasksapp.maintask.bookmarksFragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasksapp.MainActivity
import com.example.tasksapp.R
import com.example.tasksapp.adapter.SwipeEraser
import com.example.tasksapp.adapter.TagListener
import com.example.tasksapp.adapter.TagsAdapter
import com.example.tasksapp.databinding.BookmarksFragmentBinding
import com.example.tasksapp.maintask.MainTaskViewModel
import com.example.tasksapp.maintask.optionsDialog.BookMark
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class BookmarksFragment : Fragment() {

    private val viewModel by activityViewModels<MainTaskViewModel>()
    private lateinit var binding: BookmarksFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BookmarksFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        (activity as AppCompatActivity).setSupportActionBar(binding.bookmarkBar)

        val bookmarkRecyclerView = binding.tagRecyclerView
        val adapter = TagsAdapter(TagListener {
            viewModel.setTagName(it)
        })
        bookmarkRecyclerView.adapter = adapter
        bookmarkRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        bookmarkRecyclerView.layoutManager = layoutManager

        viewModel.tagName.observe(viewLifecycleOwner, {
            it?.let {
                findNavController().navigate(BookmarksFragmentDirections.actionBookmarksFragmentToTagInfoFragment(it))
                viewModel.resetTagName()
            }
        })

        binding.addTag.setOnClickListener {
            val dialog = BookMark(viewModel)
            if (childFragmentManager.findFragmentByTag(BookMark.TAG) == null) {
                dialog.show(childFragmentManager, BookMark.TAG)
            }
        }

        lifecycleScope.launch {
            viewModel.tagsList.collectLatest {
                if (it.isNullOrEmpty()) {
                    binding.emptyTagsBlob.visibility = View.VISIBLE
                    binding.noTagsText.visibility = View.VISIBLE
                } else {
                    binding.emptyTagsBlob.visibility = View.GONE
                    binding.noTagsText.visibility = View.GONE
                }
                adapter.submitList(it)
            }
        }

        val swipeEraser = object : SwipeEraser(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                (viewHolder as TagsAdapter.TagViewHolder).deleteTagValue?.let {
                    viewModel.deleteTag(it)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeEraser)
        itemTouchHelper.attachToRecyclerView(bookmarkRecyclerView)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.topbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity
        activity.setStatusBarColor(R.color.mainBackgroundTransparent, resources)
        activity.setNavigationBarColor(R.color.mainBackground, resources)
    }
}