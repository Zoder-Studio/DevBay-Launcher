package com.devbay.launcher.app

import java.util.Locale

object FuzzyMatcher {

    const val NO_MATCH = -1

    fun score(query: String, target: String): Int {
        if (query.isEmpty()) return NO_MATCH

        val queryLower = query.lowercase(Locale.getDefault())
        val targetLower = target.lowercase(Locale.getDefault())

        var score = 0
        var queryIndex = 0
        var consecutiveMatches = 0

        for (targetIndex in targetLower.indices) {
            if (queryIndex >= queryLower.length) break
            if (targetLower[targetIndex] == queryLower[queryIndex]) {
                consecutiveMatches++
                score += BASE_MATCH_SCORE + (consecutiveMatches * CONSECUTIVE_BONUS)
                if (targetIndex == 0) {
                    score += START_OF_STRING_BONUS
                }
                queryIndex++
            } else {
                consecutiveMatches = 0
            }
        }

        return if (queryIndex == queryLower.length) score else NO_MATCH
    }

    private const val BASE_MATCH_SCORE = 10
    private const val CONSECUTIVE_BONUS = 5
    private const val START_OF_STRING_BONUS = 15
}