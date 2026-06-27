package com.viniciuscoscia.kmpfullstackplayground.dsa.data

import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.Challenge
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeId
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeRepository
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.Technique

/** Static, in-memory catalog for the slice. Later backed by the Spring server / a local cache. */
class InMemoryChallengeRepository : ChallengeRepository {
    private val challenges = listOf(
        Challenge(
            id = ChallengeId("two-sum-sorted"),
            title = "Two Sum II (sorted)",
            technique = Technique.TWO_POINTERS,
            statement = "Given a sorted array and a target, return the 0-based indices of the " +
                "two numbers that add up to the target.",
            sampleInput = listOf(1, 3, 4, 5, 7, 11),
            sampleTarget = 9,
            complexity = "O(n) time, O(1) space",
        ),
    )

    override fun all(): List<Challenge> = challenges

    override fun byId(id: ChallengeId): Challenge? = challenges.firstOrNull { it.id == id }
}
