package com.cncindex.ui.`import`

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.cncindex.databinding.FragmentImportBinding
import kotlinx.coroutines.launch

class ImportFragment : Fragment() {

    private var _binding: FragmentImportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ImportViewModel by viewModels()

    private val pickIndexFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importIndex(it) }
    }

    private val pickToolsFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importTools(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnImportIndex.setOnClickListener {
            pickIndexFile.launch("application/json")
        }

        binding.btnImportTools.setOnClickListener {
            pickToolsFile.launch("application/json")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ImportUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnImportIndex.isEnabled = false
                            binding.btnImportTools.isEnabled = false
                        }
                        is ImportUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnImportIndex.isEnabled = true
                            binding.btnImportTools.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        is ImportUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnImportIndex.isEnabled = true
                            binding.btnImportTools.isEnabled = true
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnImportIndex.isEnabled = true
                            binding.btnImportTools.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
