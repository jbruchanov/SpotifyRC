package com.scurab.android.spotifyrc

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.scurab.android.spotifyrc.adapter.SearchAdapter
import com.scurab.android.spotifyrc.databinding.FragmentSearchBinding
import com.scurab.android.spotifyrc.util.viewBinding
import com.scurab.android.spotifyrc.viewmodel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val views by viewBinding { FragmentSearchBinding.bind(requireView()) }
    private val viewModel by viewModels<SearchViewModel>()
    private val adapter by viewBinding {
        SearchAdapter { viewModel.onItemClicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
        bindViews()
    }

    private fun bindViews() {
        views.buttonSearch.setOnClickListener {
            dispatchSearch()
        }

        views.searchQuery.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                dispatchSearch()
                true
            } else false
        }

        views.recyclerView.adapter = adapter
        views.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun dispatchSearch() {
        viewModel.search(views.searchQuery.text.toString(), "album", null)
    }

    private fun bindViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) {
            views.progressBarContainer.isVisible = it.isProgressVisible
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, null)
                .show()
        }

        viewModel.search.observe(viewLifecycleOwner) {
            adapter.search = it
        }
    }
}