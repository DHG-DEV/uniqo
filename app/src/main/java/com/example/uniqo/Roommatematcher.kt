package com.example.uniqo

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Modular, deterministic roommate compatibility engine.
 *
 * Weights mirror the product spec:
 *   budget 20%, sleep schedule 15%, smoking 15%, food 10%,
 *   cleanliness 15%, pets 10%, study style 15%.
 * Distance is applied as a soft multiplier on top of the weighted trait score,
 * so it influences ranking without being a hard cutoff.
 *
 * This is intentionally isolated behind [score] / [rank] so it can later be
 * swapped for a learned/AI ranking model without touching UI code.
 */
object RoommateMatcher {

    private const val WEIGHT_BUDGET = 0.20
    private const val WEIGHT_SLEEP = 0.15
    private const val WEIGHT_SMOKING = 0.15
    private const val WEIGHT_FOOD = 0.10
    private const val WEIGHT_CLEAN = 0.15
    private const val WEIGHT_PETS = 0.10
    private const val WEIGHT_STUDY = 0.15

    /** Overlap ratio of two [min,max] ranges, 0..1. */
    private fun budgetScore(a: RoommatePreferences, b: RoommatePreferences): Double {
        val overlapStart = max(a.budgetMin, b.budgetMin)
        val overlapEnd = min(a.budgetMax, b.budgetMax)
        val overlap = max(0, overlapEnd - overlapStart)
        val unionSpan = max(a.budgetMax, b.budgetMax) - min(a.budgetMin, b.budgetMin)
        if (unionSpan <= 0) return 1.0
        return (overlap.toDouble() / unionSpan).coerceIn(0.0, 1.0)
    }

    private fun sleepScore(a: RoommatePreferences, b: RoommatePreferences): Double =
        if (a.sleepSchedule == b.sleepSchedule) 1.0
        else if (a.sleepSchedule.name == "FLEXIBLE" || b.sleepSchedule.name == "FLEXIBLE") 0.7
        else 0.2

    private fun smokingScore(a: RoommatePreferences, b: RoommatePreferences): Double =
        if (a.smoking == b.smoking) 1.0 else 0.0

    private fun foodScore(a: RoommatePreferences, b: RoommatePreferences): Double =
        when {
            a.food == b.food -> 1.0
            a.food.name == "EITHER" || b.food.name == "EITHER" -> 0.8
            else -> 0.3
        }

    private fun cleanlinessScore(a: RoommatePreferences, b: RoommatePreferences): Double {
        val order = listOf(CleanlinessLevel.LOW, CleanlinessLevel.MEDIUM, CleanlinessLevel.HIGH)
        val diff = kotlin.math.abs(order.indexOf(a.cleanliness) - order.indexOf(b.cleanliness))
        return when (diff) {
            0 -> 1.0
            1 -> 0.6
            else -> 0.2
        }
    }

    private fun petsScore(a: RoommatePreferences, b: RoommatePreferences): Double =
        if (a.pets == b.pets) 1.0 else 0.4

    private fun studyScore(a: RoommatePreferences, b: RoommatePreferences): Double {
        val order = listOf(StudyEnvironment.QUIET, StudyEnvironment.MODERATE, StudyEnvironment.SOCIAL)
        val diff = kotlin.math.abs(order.indexOf(a.studyEnvironment) - order.indexOf(b.studyEnvironment))
        return when (diff) {
            0 -> 1.0
            1 -> 0.55
            else -> 0.15
        }
    }

    /** Distance multiplier: full credit within preferred radius, tapering off beyond it. */
    private fun distanceMultiplier(distanceKm: Double, maxPreferredKm: Double): Double {
        if (maxPreferredKm <= 0) return 1.0
        val ratio = distanceKm / maxPreferredKm
        return when {
            ratio <= 1.0 -> 1.0
            ratio <= 2.0 -> 0.9
            ratio <= 3.0 -> 0.75
            else -> 0.6
        }
    }

    /**
     * Returns a 0..100 compatibility score plus a per-factor breakdown (each already
     * weighted, so the breakdown values sum to the total before the distance multiplier).
     */
    fun score(mine: RoommatePreferences, candidate: RoommateCandidate): RoommateMatch {
        val other = candidate.preferences

        val budget = budgetScore(mine, other) * WEIGHT_BUDGET
        val sleep = sleepScore(mine, other) * WEIGHT_SLEEP
        val smoking = smokingScore(mine, other) * WEIGHT_SMOKING
        val food = foodScore(mine, other) * WEIGHT_FOOD
        val clean = cleanlinessScore(mine, other) * WEIGHT_CLEAN
        val pets = petsScore(mine, other) * WEIGHT_PETS
        val study = studyScore(mine, other) * WEIGHT_STUDY

        val rawTotal = budget + sleep + smoking + food + clean + pets + study
        val multiplier = distanceMultiplier(candidate.distanceKm, mine.maxDistanceKm)
        val finalPercent = (rawTotal * multiplier * 100).roundToInt().coerceIn(0, 100)

        val breakdown = linkedMapOf(
            "Budget" to (budget * 100).roundToInt(),
            "Sleep schedule" to (sleep * 100).roundToInt(),
            "Smoking" to (smoking * 100).roundToInt(),
            "Food" to (food * 100).roundToInt(),
            "Cleanliness" to (clean * 100).roundToInt(),
            "Pets" to (pets * 100).roundToInt(),
            "Study style" to (study * 100).roundToInt()
        )

        return RoommateMatch(candidate = candidate, scorePercent = finalPercent, breakdown = breakdown)
    }

    /** Ranks all candidates against the user's preferences, best match first. */
    fun rank(mine: RoommatePreferences, candidates: List<RoommateCandidate>): List<RoommateMatch> =
        candidates.map { score(mine, it) }.sortedByDescending { it.scorePercent }
}