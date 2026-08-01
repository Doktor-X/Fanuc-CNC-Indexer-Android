package com.cncindex.ui.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import androidx.core.content.ContextCompat
import com.cncindex.R
import com.cncindex.data.local.entity.Tool
import com.cncindex.databinding.ItemToolBinding

class ToolAdapter : ListAdapter<Tool, ToolAdapter.ViewHolder>(DIFF) {

    private val installedTools = mutableSetOf<Int>()

    inner class ViewHolder(private val binding: ItemToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool) {
            binding.tvToolNumber.text = "T${tool.toolNumber}"
            binding.tvToolName.text = tool.name
            showInstalledState(tool.toolNumber)
            binding.root.setOnClickListener {
                if (!installedTools.add(tool.toolNumber)) {
                    installedTools.remove(tool.toolNumber)
                }
                showInstalledState(tool.toolNumber)
            }
        }

        private fun showInstalledState(toolNumber: Int) {
            val installed = toolNumber in installedTools
            val context = binding.root.context
            if (installed) {
                val green = ContextCompat.getColor(context, R.color.tool_installed)
                val white = ContextCompat.getColor(context, R.color.on_tool_installed)
                binding.root.setBackgroundColor(green)
                binding.cardNumber.setCardBackgroundColor(green)
                binding.tvToolNumber.setTextColor(white)
                binding.tvToolName.setTextColor(white)
            } else {
                binding.root.setBackgroundColor(Color.TRANSPARENT)
                binding.cardNumber.setCardBackgroundColor(
                    MaterialColors.getColor(binding.cardNumber, com.google.android.material.R.attr.colorPrimaryContainer)
                )
                binding.tvToolNumber.setTextColor(
                    MaterialColors.getColor(binding.tvToolNumber, com.google.android.material.R.attr.colorOnPrimaryContainer)
                )
                binding.tvToolName.setTextColor(
                    MaterialColors.getColor(binding.tvToolName, com.google.android.material.R.attr.colorOnSurface)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemToolBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Tool>() {
            override fun areItemsTheSame(a: Tool, b: Tool) = a.toolNumber == b.toolNumber
            override fun areContentsTheSame(a: Tool, b: Tool) = a == b
        }
    }
}
