package com.devbay.launcher.github

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GitHubSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val apiClient = GitHubApiClient()

    override suspend fun doWork(): Result {
        val preferences = GitHubPreferences(applicationContext)
        val token = preferences.getToken()
        val watchedRepos = preferences.getWatchedRepos()
        if (watchedRepos.isEmpty()) return Result.success()

        GitHubNotificationHelper.ensureChannel(applicationContext)

        watchedRepos.forEach { repo ->
            val newCount = apiClient.fetchOpenIssueAndPrCount(repo.owner, repo.repo, token) ?: return@forEach
            val previousCount = repo.lastKnownOpenCount

            preferences.updateRepoCount(repo.owner, repo.repo, newCount)

            if (previousCount >= 0 && newCount > previousCount) {
                GitHubNotificationHelper.notifyNewItems(applicationContext, repo, newCount - previousCount)
            }
        }

        return Result.success()
    }
}