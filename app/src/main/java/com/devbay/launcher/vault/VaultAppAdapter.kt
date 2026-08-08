package com.devbay.launcher.vault

import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import com.devbay.launcher.app.*
import com.devbay.launcher.notification.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemAppBinding

class VaultAppAdapter(
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<VaultAppAdapter.VaultAppViewHolder>() {

    inner class VaultAppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultAppViewHolder {
        val binding = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VaultAppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaultAppViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.appIcon.setImageDrawable(app.icon)
        holder.binding.appLabel.text = app.label

        val badgeCount = NotificationBadgeStore.getCounts()[app.packageName] ?: 0
        if (badgeCount > 0) {
            holder.binding.appBadge.visibility = View.VISIBLE
            holder.binding.appBadge.text = if (badgeCount > MAX_BADGE_COUNT) {
                holder.binding.root.context.getString(R.string.badge_overflow)
            } else {
                badgeCount.toString()
            }
        } else {
            holder.binding.appBadge.visibility = View.GONE
        }

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

    companion object {
        private const val MAX_BADGE_COUNT = 99
    }
}