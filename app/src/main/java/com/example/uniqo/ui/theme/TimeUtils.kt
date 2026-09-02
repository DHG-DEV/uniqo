package com.example.uniqo

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/**
 * Turns a Supabase timestamptz string (e.g. "2026-08-26T05:11:20.087731+00:00")
 * into a friendly relative label like "Just now", "5m ago", "3h ago", "2d ago".
 * Falls back to "Recently" if the timestamp is missing or unparseable.
 */
fun relativeTimeFrom(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "Recently"
    return try {
        val postedInstant = try {
            Instant.parse(createdAt)
        } catch (e: DateTimeParseException) {
            OffsetDateTime.parse(createdAt).toInstant()
        }
        val seconds = Duration.between(postedInstant, Instant.now()).seconds.coerceAtLeast(0)
        when {
            seconds < 60 -> "Just now"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86_400 -> "${seconds / 3600}h ago"
            seconds < 86_400 * 7 -> "${seconds / 86_400}d ago"
            seconds < 86_400 * 30 -> "${seconds / (86_400 * 7)}w ago"
            else -> "${seconds / (86_400 * 30)}mo ago"
        }
    } catch (e: DateTimeParseException) {
        "Recently"
    }
}