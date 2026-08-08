package com.devbay.launcher.icon

import com.devbay.launcher.databinding.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemIconPackBinding

class IconPackPickerAdapter(
    private val items: List<IconPackPickerItem>,
    private val onItemClick: (IconPackPickerItem) -> Unit
) : RecyclerView.Adapter<IconPackPickerAdapter.IconPackViewHolder>() {

    inner class IconPackViewHolder(val binding: ItemIconPackBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconPackViewHolder {
        val binding = ItemIconPackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconPackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconPackViewHolder, position: Int) {
        val item = items[position]
        holder.binding.iconPackLabel.text = item.label
        holder.binding.iconPackCheckmark.visibility = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}