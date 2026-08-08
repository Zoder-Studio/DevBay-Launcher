package com.devbay.launcher.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.devbay.launcher.R
import com.devbay.launcher.databinding.*
import com.devbay.launcher.github.*
import com.devbay.launcher.databinding.ActivityGithubMonitorBinding

class GitHubMonitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGithubMonitorBinding
    private lateinit var preferences: GitHubPreferences
    private lateinit var repoAdapter: GitHubRepoAdapter

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGithubMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = GitHubPreferences(applicationContext)

        binding.closeButton.setOnClickListener { finish() }
        binding.tokenInput.setText(preferences.getToken().orEmpty())
        binding.saveTokenButton.setOnClickListener { saveToken() }
        binding.addRepoButton.setOnClickListener { addRepo() }
        binding.syncNowButton.setOnClickListener {
            GitHubSyncScheduler.syncNow(applicationContext)
            Toast.makeText(this, R.string.github_sync_started, Toast.LENGTH_SHORT).show()
        }

        repoAdapter = GitHubRepoAdapter(emptyList()) { repo -> removeRepo(repo) }
        binding.repoList.layoutManager = LinearLayoutManager(this)
        binding.repoList.adapter = repoAdapter

        requestNotificationPermissionIfNeeded()
        refreshRepoList()
    }

    override fun onResume() {
        super.onResume()
        refreshRepoList()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun saveToken() {
        val token = binding.tokenInput.text.toString().trim()
        if (token.isBlank()) {
            Toast.makeText(this, R.string.github_token_empty, Toast.LENGTH_SHORT).show()
            return
        }
        preferences.setToken(token)
        Toast.makeText(this, R.string.github_token_saved, Toast.LENGTH_SHORT).show()
    }

    private fun addRepo() {
        val raw = binding.repoInput.text.toString().trim()
        val parts = raw.split("/")
        if (parts.size != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            Toast.makeText(this, R.string.github_invalid_repo_format, Toast.LENGTH_SHORT).show()
            return
        }
        preferences.addRepo(parts[0], parts[1])
        binding.repoInput.text?.clear()
        GitHubSyncScheduler.ensureScheduled(applicationContext)
        refreshRepoList()
    }

    private fun removeRepo(repo: GitHubWatchedRepo) {
        preferences.removeRepo(repo.owner, repo.repo)
        GitHubSyncScheduler.ensureScheduled(applicationContext)
        refreshRepoList()
    }

    private fun refreshRepoList() {
        val repos = preferences.getWatchedRepos()
        repoAdapter.updateRepos(repos)
        binding.emptyRepoState.visibility =
            if (repos.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}