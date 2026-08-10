package com.devbay.launcher.widget

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemWidgetProviderBinding

class WidgetPickerAdapter(
    private val providers: List<WidgetProviderEntry>,
    private val onProviderClick: (WidgetProviderEntry) -> Unit
) : RecyclerView.Adapter<WidgetPickerAdapter.ProviderViewHolder>() {

    inner class ProviderViewHolder(val binding: ItemWidgetProviderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val binding = ItemWidgetProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProviderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val entry = providers[position]
        val context = holder.binding.root.context
        val icon = entry.providerInfo.loadIcon(context, DisplayMetrics.DENSITY_DEFAULT)
        holder.binding.providerIcon.setImageDrawable(icon)
        holder.binding.providerLabel.text = entry.label

        val preview = entry.providerInfo.loadPreviewImage(context, DisplayMetrics.DENSITY_DEFAULT)
        if (preview != null) {
            holder.binding.providerPreview.setImageDrawable(preview)
            holder.binding.providerPreview.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.providerPreview.visibility = android.view.View.GONE
        }

        holder.binding.root.setOnClickListener { onProviderClick(entry) }
    }

    override fun getItemCount(): Int = providers.size
}