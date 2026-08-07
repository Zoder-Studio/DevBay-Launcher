package com.devbay.launcher.github

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class GitHubApiClient {

    suspend fun fetchOpenIssueAndPrCount(owner: String, repo: String, token: String?): Int? {
        return withContext(Dispatchers.IO) {
            val issueCount = fetchCount(owner, repo, token, "issue")
            val prCount = fetchCount(owner, repo, token, "pr")
            if (issueCount == null || prCount == null) null else issueCount + prCount
        }
    }

    private fun fetchCount(owner: String, repo: String, token: String?, type: String): Int? {
        val query = URLEncoder.encode("repo:$owner/$repo type:$type state:open", "UTF-8")
        val url = URL("https://api.github.com/search/issues?q=$query&per_page=1")

        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            if (!token.isNullOrBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $token")
            }
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                connection.disconnect()
                return null
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            JSONObject(body).optInt("total_count", -1).takeIf { it >= 0 }
        } catch (throwable: Throwable) {
            null
        }
    }

    companion object {
        private const val TIMEOUT_MS = 15_000
    }
}