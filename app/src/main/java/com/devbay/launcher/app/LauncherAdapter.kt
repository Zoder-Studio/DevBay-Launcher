package com.devbay.launcher.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemAppBinding
import com.devbay.launcher.databinding.ItemSectionHeaderBinding

class LauncherAdapter(
    private var items: List<LauncherListItem>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo) -> Unit,
    private val onEditPinnedClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class HeaderViewHolder(val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    inner class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is LauncherListItem.Header -> VIEW_TYPE_HEADER
            is LauncherListItem.AppItem -> VIEW_TYPE_APP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(ItemSectionHeaderBinding.inflate(inflater, parent, false))
        } else {
            AppViewHolder(ItemAppBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is LauncherListItem.Header -> bindHeader(holder as HeaderViewHolder, item)
            is LauncherListItem.AppItem -> bindApp(holder as AppViewHolder, item.app)
        }
    }

    private fun bindHeader(holder: HeaderViewHolder, header: LauncherListItem.Header) {
        val context = holder.binding.root.context
        holder.binding.sectionTitle.text = header.category.label
        holder.binding.sectionCount.text = if (header.count == 1) {
            context.getString(R.string.app_count_one)
        } else {
            context.getString(R.string.app_count_other, header.count)
        }

        if (header.category == AppCategory.PINNED) {
            holder.binding.sectionEdit.visibility = View.VISIBLE
            holder.binding.sectionEdit.setOnClickListener { onEditPinnedClick() }
        } else {
            holder.binding.sectionEdit.visibility = View.GONE
            holder.binding.sectionEdit.setOnClickListener(null)
        }
    }

    private fun bindApp(holder: AppViewHolder, app: AppInfo) {
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

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<LauncherListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getSpanSize(position: Int, totalSpanCount: Int): Int {
        return when (items[position]) {
            is LauncherListItem.Header -> totalSpanCount
            is LauncherListItem.AppItem -> 1
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_APP = 1
        private const val MAX_BADGE_COUNT = 99
    }
}