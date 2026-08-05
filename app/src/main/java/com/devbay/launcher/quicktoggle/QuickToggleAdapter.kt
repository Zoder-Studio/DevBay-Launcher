package com.devbay.launcher.quicktoggle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemQuickToggleBinding

class QuickToggleAdapter(
    private var chips: List<QuickToggleChip>,
    private val onChipClick: (QuickToggleChip) -> Unit
) : RecyclerView.Adapter<QuickToggleAdapter.ChipViewHolder>() {

    inner class ChipViewHolder(val binding: ItemQuickToggleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val binding = ItemQuickToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val chip = chips[position]
        holder.binding.chipIcon.setImageResource(chip.iconRes)
        holder.binding.chipLabel.text = chip.label
        holder.binding.chipCircle.isSelected = chip.isToggle && chip.isActive
        holder.binding.root.setOnClickListener { onChipClick(chip) }
    }

    override fun getItemCount(): Int = chips.size

    fun updateChips(newChips: List<QuickToggleChip>) {
        chips = newChips
        notifyDataSetChanged()
    }
}