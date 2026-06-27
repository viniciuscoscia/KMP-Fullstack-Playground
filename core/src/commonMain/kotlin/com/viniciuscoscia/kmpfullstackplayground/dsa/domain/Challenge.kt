package com.viniciuscoscia.kmpfullstackplayground.dsa.domain

/** Identifies a challenge in the catalog (stable, human-readable slug). */
data class ChallengeId(val value: String)

/** The algorithmic technique a challenge demonstrates; drives the algorithm + renderer chosen. */
enum class Technique { TWO_POINTERS, SLIDING_WINDOW, PREFIX_SUM, HASHMAP, SORTING }

/**
 * A DSA challenge: its problem statement plus a concrete, traceable sample so the
 * visualizer always has something to animate.
 */
data class Challenge(
    val id: ChallengeId,
    val title: String,
    val technique: Technique,
    val statement: String,
    val sampleInput: List<Int>,
    val sampleTarget: Int,
    val complexity: String,
)

/** Read port over the challenge catalog. Implemented in the data layer. */
interface ChallengeRepository {
    fun all(): List<Challenge>
    fun byId(id: ChallengeId): Challenge?
}
