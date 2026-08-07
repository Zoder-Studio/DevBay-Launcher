package com.devbay.launcher.github

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devbay.launcher.databinding.ItemGithubRepoHomeBinding

class GitHubHomeAdapter(
    private var repos: List<GitHubWatchedRepo>,
    private val onRepoClick: (GitHubWatchedRepo) -> Unit
) : RecyclerView.Adapter<GitHubHomeAdapter.HomeRepoViewHolder>() {

    inner class HomeRepoViewHolder(val binding: ItemGithubRepoHomeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRepoViewHolder {
        val binding = ItemGithubRepoHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeRepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeRepoViewHolder, position: Int) {
        val repo = repos[position]
        holder.binding.repoHomeName.text = repo.repo
        holder.binding.repoHomeCount.text = if (repo.lastKnownOpenCount >= 0) {
            repo.lastKnownOpenCount.toString()
        } else {
            "…"
        }
        holder.binding.root.setOnClickListener { onRepoClick(repo) }
    }

    override fun getItemCount(): Int = repos.size

    fun updateRepos(newRepos: List<GitHubWatchedRepo>) {
        repos = newRepos
        notifyDataSetChanged()
    }
}