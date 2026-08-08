package com.devbay.launcher.github

import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemGithubRepoManageBinding

class GitHubRepoAdapter(
    private var repos: List<GitHubWatchedRepo>,
    private val onRemoveClick: (GitHubWatchedRepo) -> Unit
) : RecyclerView.Adapter<GitHubRepoAdapter.RepoViewHolder>() {

    inner class RepoViewHolder(val binding: ItemGithubRepoManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = ItemGithubRepoManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repos[position]
        holder.binding.repoNameText.text = repo.fullName
        holder.binding.repoCountText.text = if (repo.lastKnownOpenCount >= 0) {
            holder.binding.root.context.getString(R.string.github_open_count_format, repo.lastKnownOpenCount)
        } else {
            holder.binding.root.context.getString(R.string.github_not_synced_yet)
        }
        holder.binding.removeRepoButton.setOnClickListener { onRemoveClick(repo) }
    }

    override fun getItemCount(): Int = repos.size

    fun updateRepos(newRepos: List<GitHubWatchedRepo>) {
        repos = newRepos
        notifyDataSetChanged()
    }
}