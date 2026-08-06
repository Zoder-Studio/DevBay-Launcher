package com.devbay.launcher.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemSettingsShortcutBinding

class SettingsShortcutAdapter(
    private val shortcuts: List<SettingsShortcut>,
    private val onShortcutClick: (SettingsShortcut) -> Unit
) : RecyclerView.Adapter<SettingsShortcutAdapter.ShortcutViewHolder>() {

    inner class ShortcutViewHolder(val binding: ItemSettingsShortcutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val binding = ItemSettingsShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShortcutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        val shortcut = shortcuts[position]
        holder.binding.shortcutIcon.setImageResource(shortcut.iconRes)
        holder.binding.shortcutLabel.text = shortcut.label
        holder.binding.root.setOnClickListener { onShortcutClick(shortcut) }
    }

    override fun getItemCount(): Int = shortcuts.size
}