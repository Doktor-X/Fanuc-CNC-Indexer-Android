package com.cncindex.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cncindex.data.local.entity.CncProgram
import com.cncindex.data.repository.ProgramGroup
import com.cncindex.databinding.ItemProgramBinding
import com.cncindex.databinding.ItemProgramChildBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProgramAdapter(
    private val onClick: (CncProgram) -> Unit
) : ListAdapter<ProgramGroup, RecyclerView.ViewHolder>(DIFF) {

    private val gson = Gson()

    companion object {
        private const val VIEW_NORMAL = 0
        private const val VIEW_DUPLICATE_GROUP = 1

        private val DIFF = object : DiffUtil.ItemCallback<ProgramGroup>() {
            override fun areItemsTheSame(a: ProgramGroup, b: ProgramGroup) =
                a.master.id == b.master.id
            override fun areContentsTheSame(a: ProgramGroup, b: ProgramGroup) = a == b
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).isDuplicateGroup) VIEW_DUPLICATE_GROUP else VIEW_NORMAL

    inner class NormalViewHolder(private val binding: ItemProgramBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(program: CncProgram) {
            binding.tvProgramNumber.text = "O${program.programNumber ?: program.filename}"
            binding.tvProgramName.text = program.programName ?: "-"

            val type = object : TypeToken<List<Int>>() {}.type
            val toolIds: List<Int> = gson.fromJson(program.toolsJson, type) ?: emptyList()
            binding.tvToolCount.text = "${toolIds.size} alata"

            binding.badgeProblem.visibility = if (program.hasProblem) View.VISIBLE else View.GONE
            binding.badgeDuplicate.visibility = View.GONE
            binding.root.setCardBackgroundColor(
                if (program.hasProblem) 0xFFFFEBEE.toInt() else 0xFFFFFFFF.toInt()
            )
            binding.root.setOnClickListener { onClick(program) }
        }
    }

    inner class DuplicateGroupViewHolder(private val binding: ItemProgramChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: ProgramGroup) {
            val master = group.master
            binding.tvMasterNumber.text = "O${master.programNumber ?: master.filename}"
            binding.tvMasterName.text = master.programName ?: "-"

            val type = object : TypeToken<List<Int>>() {}.type
            val toolIds: List<Int> = gson.fromJson(master.toolsJson, type) ?: emptyList()
            binding.tvMasterToolCount.text = "${toolIds.size} alata"
            binding.tvDupCount.text = "×${group.children.size + 1}"
            binding.badgeMasterProblem.visibility =
                if (master.hasProblem) View.VISIBLE else View.GONE
            binding.masterRow.setOnClickListener { onClick(master) }

            binding.childrenContainer.removeAllViews()
            for (child in group.children) {
                val childView = LayoutInflater.from(binding.root.context)
                    .inflate(com.cncindex.R.layout.item_program_child_row, binding.childrenContainer, false)
                childView.findViewById<android.widget.TextView>(com.cncindex.R.id.tv_child_filename)
                    .text = child.filename
                childView.findViewById<android.widget.TextView>(com.cncindex.R.id.tv_child_name)
                    .text = child.programName ?: "-"
                childView.findViewById<android.widget.TextView>(com.cncindex.R.id.badge_child_problem)
                    .visibility = if (child.hasProblem) View.VISIBLE else View.GONE
                childView.setOnClickListener { onClick(child) }
                binding.childrenContainer.addView(childView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_NORMAL) {
            NormalViewHolder(ItemProgramBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            DuplicateGroupViewHolder(ItemProgramChildBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val group = getItem(position)
        when (holder) {
            is NormalViewHolder -> holder.bind(group.master)
            is DuplicateGroupViewHolder -> holder.bind(group)
        }
    }
}
