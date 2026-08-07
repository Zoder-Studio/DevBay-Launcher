package com.devbay.launcher.github

data class GitHubWatchedRepo(
    val owner: String,
    val repo: String,
    val lastKnownOpenCount: Int = -1
) {
    val fullName: String get() = "$owner/$repo"
}