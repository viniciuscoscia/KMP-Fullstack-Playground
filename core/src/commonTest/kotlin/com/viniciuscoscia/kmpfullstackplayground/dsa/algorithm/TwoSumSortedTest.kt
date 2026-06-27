package com.viniciuscoscia.kmpfullstackplayground.dsa.algorithm

import com.viniciuscoscia.kmpfullstackplayground.dsa.trace.RecordingTracer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * Multiplatform tests (kotlin.test). Names are camelCase rather than backticked-with-spaces
 * because space-in-name functions break the JS/Wasm test targets.
 */
class TwoSumSortedTest {
    private val solution = TwoSumSorted()

    @Test
    fun findsAdjacentPairAtStart() {
        val numbers = listOf(2, 7, 11, 15)
        val result = solution.solve(numbers, 9)
        println("Input: $numbers target=9 -> ${result.toList()}")
        assertContentEquals(intArrayOf(0, 1), result)
    }

    @Test
    fun findsPairAtTheEnds() {
        val numbers = listOf(1, 2, 3, 4, 6)
        val result = solution.solve(numbers, 7) // 1 + 6
        assertContentEquals(intArrayOf(0, 4), result)
    }

    @Test
    fun findsPairConvergingInTheMiddle() {
        val numbers = listOf(1, 3, 4, 5, 7, 11)
        val result = solution.solve(numbers, 9) // 4 + 5 -> indices 2, 3
        assertContentEquals(intArrayOf(2, 3), result)
    }

    @Test
    fun returnsMinusOneWhenNoPairExists() {
        val numbers = listOf(1, 2, 3, 9)
        val result = solution.solve(numbers, 100)
        assertContentEquals(intArrayOf(-1, -1), result)
    }

    @Test
    fun handlesTwoElementInput() {
        assertContentEquals(intArrayOf(0, 1), solution.solve(listOf(5, 5), 10))
    }

    @Test
    fun recordingTracerCapturesStepsEndingAtTheSolution() {
        val numbers = listOf(1, 2, 3, 4, 6)
        val tracer = RecordingTracer()
        val result = solution.solve(numbers, 10, tracer) // 4 + 6 -> indices 3, 4
        val trace = tracer.build()

        assertContentEquals(intArrayOf(3, 4), result)
        assertTrue(trace.size > 1, "expected several recorded steps")
        assertTrue(trace.steps.last().narration.contains("Found"))
        assertTrue(trace.steps.last().metrics.comparisons >= 1)
    }

    @Test
    fun defaultRunMatchesRecordedRun() {
        val numbers = listOf(1, 2, 3, 4, 6)
        val plain = solution.solve(numbers, 10)
        val traced = solution.solve(numbers, 10, RecordingTracer())
        assertContentEquals(plain, traced)
    }
}
