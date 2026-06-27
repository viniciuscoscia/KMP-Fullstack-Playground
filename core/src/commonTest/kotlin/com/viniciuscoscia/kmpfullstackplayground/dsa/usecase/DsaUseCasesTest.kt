package com.viniciuscoscia.kmpfullstackplayground.dsa.usecase

import com.viniciuscoscia.kmpfullstackplayground.dsa.data.InMemoryChallengeRepository
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DsaUseCasesTest {
    private val repository = InMemoryChallengeRepository()

    @Test
    fun listsCatalog() {
        assertEquals(1, ListChallenges(repository)().size)
    }

    @Test
    fun getsKnownChallengeAndMissesUnknown() {
        assertNotNull(GetChallenge(repository)(ChallengeId("two-sum-sorted")))
        assertNull(GetChallenge(repository)(ChallengeId("does-not-exist")))
    }

    @Test
    fun generatesTraceEndingAtTheSolution() {
        val trace = GenerateTrace(repository)(ChallengeId("two-sum-sorted"))
        assertTrue(trace.size > 1, "expected a multi-step trace")
        assertTrue(trace.steps.last().narration.contains("Found"))
    }
}
