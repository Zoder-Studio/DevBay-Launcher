package com.devbay.launcher.github

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject

class GitHubPreferences(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getToken(): String? = preferences.getString(KEY_TOKEN, null)

    fun setToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply()
    }

    fun getWatchedRepos(): List<GitHubWatchedRepo> {
        val raw = preferences.getString(KEY_REPOS, "") ?: ""
        if (raw.isBlank()) return emptyList()

        val jsonArray = JSONArray(raw)
        return (0 until jsonArray.length()).mapNotNull { index ->
            val obj = jsonArray.optJSONObject(index) ?: return@mapNotNull null
            val owner = obj.optString(FIELD_OWNER)
            val repo = obj.optString(FIELD_REPO)
            val count = obj.optInt(FIELD_COUNT, -1)
            if (owner.isBlank() || repo.isBlank()) null else GitHubWatchedRepo(owner, repo, count)
        }
    }

    fun addRepo(owner: String, repo: String) {
        val current = getWatchedRepos().toMutableList()
        if (current.none { it.owner.equals(owner, true) && it.repo.equals(repo, true) }) {
            current.add(GitHubWatchedRepo(owner, repo))
            saveRepos(current)
        }
    }

    fun removeRepo(owner: String, repo: String) {
        val current = getWatchedRepos().filterNot { it.owner.equals(owner, true) && it.repo.equals(repo, true) }
        saveRepos(current)
    }

    fun updateRepoCount(owner: String, repo: String, newCount: Int) {
        val current = getWatchedRepos().map {
            if (it.owner.equals(owner, true) && it.repo.equals(repo, true)) it.copy(lastKnownOpenCount = newCount) else it
        }
        saveRepos(current)
    }

    private fun saveRepos(repos: List<GitHubWatchedRepo>) {
        val jsonArray = JSONArray()
        repos.forEach { repo ->
            val obj = JSONObject()
            obj.put(FIELD_OWNER, repo.owner)
            obj.put(FIELD_REPO, repo.repo)
            obj.put(FIELD_COUNT, repo.lastKnownOpenCount)
            jsonArray.put(obj)
        }
        preferences.edit().putString(KEY_REPOS, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "devbay_github_secure"
        private const val KEY_TOKEN = "github_token"
        private const val KEY_REPOS = "watched_repos"
        private const val FIELD_OWNER = "owner"
        private const val FIELD_REPO = "repo"
        private const val FIELD_COUNT = "count"
    }
}