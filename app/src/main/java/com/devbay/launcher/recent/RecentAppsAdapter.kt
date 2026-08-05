package com.devbay.launcher.recent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemRecentAppBinding

class RecentAppsAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<RecentAppsAdapter.RecentAppViewHolder>() {

    inner class RecentAppViewHolder(val binding: ItemRecentAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAppViewHolder {
        val binding = ItemRecentAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentAppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentAppViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.recentAppIcon.setImageDrawable(app.icon)
        holder.binding.root.setOnClickListener { onAppClick(app) }
        holder.binding.root.setOnLongClickListener {
            onAppLongClick(app)
            true
        }
    }

    override fun getItemCount(): Int = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}