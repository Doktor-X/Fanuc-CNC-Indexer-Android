package com.cncindex.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cncindex.data.local.database.AppDatabase
import com.cncindex.databinding.FragmentDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private lateinit var toolAdapter: ToolAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolAdapter = ToolAdapter()
        binding.recyclerTools.adapter = toolAdapter

        val programId = arguments?.getLong("programId") ?: return
        val db = AppDatabase.getInstance(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            val program = withContext(Dispatchers.IO) {
                db.programDao().getProgramById(programId)
            }
            program?.let { viewModel.load(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (!state.isLoading) {
                        state.program?.let { p ->
                            binding.tvFilename.text = p.filename
                            binding.tvProgramNumber.text = p.programNumber?.let { "O$it" } ?: "-"
                            binding.tvProgramName.text = p.programName ?: "-"

                            // Putanja na PC-u
                            if (!p.filepath.isNullOrBlank()) {
                                binding.tvFilepath.text = p.filepath
                                binding.tvFilepath.visibility = View.VISIBLE
                            } else {
                                binding.tvFilepath.visibility = View.GONE
                            }

                            // Datum zadnje izmjene (Unix timestamp u sekundama)
                            if (p.modified > 0) {
                                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                binding.tvModified.text = sdf.format(Date(p.modified * 1000L))
                            } else {
                                binding.tvModified.text = "-"
                            }

                            // Oznake problema i duplikata
                            val showBadge = p.hasProblem || p.isDuplicate
                            binding.badgeRow.visibility = if (showBadge) View.VISIBLE else View.GONE
                            binding.badgeProblemDetail.visibility =
                                if (p.hasProblem) View.VISIBLE else View.GONE
                            binding.badgeDuplicateDetail.visibility =
                                if (p.isDuplicate) View.VISIBLE else View.GONE
                        }
                        toolAdapter.submitList(state.tools)
                        binding.tvNoTools.visibility =
                            if (state.tools.isEmpty()) View.VISIBLE else View.GONE
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
