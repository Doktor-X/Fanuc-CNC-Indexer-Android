package com.cncindex.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cncindex.R
import com.cncindex.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: ProgramAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProgramAdapter { program ->
            val bundle = Bundle().apply {
                putLong("programId", program.id)
                putString("programTitle", program.programName ?: program.programNumber ?: program.filename)
            }
            findNavController().navigate(R.id.action_searchFragment_to_detailFragment, bundle)
        }

        binding.recyclerView.adapter = adapter

        binding.searchInput.doAfterTextChanged { text ->
            viewModel.setQuery(text?.toString() ?: "")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.groups.collect { groups ->
                    adapter.submitList(groups)
                    val total = groups.sumOf { 1 + it.children.size }
                    binding.tvEmpty.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvResultCount.text =
                        resources.getQuantityString(R.plurals.result_count, total, total)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
