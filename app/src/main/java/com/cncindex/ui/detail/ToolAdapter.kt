package com.cncindex.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cncindex.data.local.entity.Tool
import com.cncindex.databinding.ItemToolBinding

class ToolAdapter : ListAdapter<Tool, ToolAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemToolBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tool: Tool) {
            binding.tvToolNumber.text = "T${tool.toolNumber}"
            binding.tvToolName.text = tool.name
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
