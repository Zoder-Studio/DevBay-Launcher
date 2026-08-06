package com.devbay.launcher.clipboard

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemClipboardEntryBinding

class ClipboardAdapter(
    private var entries: List<ClipboardEntry>,
    private val onEntryClick: (ClipboardEntry) -> Unit,
    private val onEntryLongClick: (ClipboardEntry) -> Unit
) : RecyclerView.Adapter<ClipboardAdapter.EntryViewHolder>() {

    inner class EntryViewHolder(val binding: ItemClipboardEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemClipboardEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.binding.clipText.text = entry.text
        holder.binding.clipTime.text = DateUtils.getRelativeTimeSpanString(
            entry.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.binding.root.setOnClickListener { onEntryClick(entry) }
        holder.binding.root.setOnLongClickListener {
            onEntryLongClick(entry)
            true
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<ClipboardEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}