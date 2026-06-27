package com.viniciuscoscia.kmpfullstackplayground.dsa.algorithm

import com.viniciuscoscia.kmpfullstackplayground.dsa.trace.NoOpTracer
import com.viniciuscoscia.kmpfullstackplayground.dsa.trace.Tracer

/**
 * Two Sum II (sorted input).
 *
 * Given [numbers] sorted in non-decreasing order and a [target], return the 0-based indices
 * of the two values that add up to [target], or `[-1, -1]` if none exist.
 *
 * Two pointers converging from both ends: O(n) time, O(1) space.
 *
 * Pass a [Tracer] to capture an animation trace. The default [NoOpTracer] keeps the
 * canonical run free of instrumentation cost.
 */
class TwoSumSorted {
    fun solve(numbers: List<Int>, target: Int, tracer: Tracer = NoOpTracer): IntArray {
        var left = 0
        var right = numbers.lastIndex
        tracer.step(
            array = numbers,
            pointers = mapOf("left" to left, "right" to right),
            narration = "Start: left at 0, right at ${numbers.lastIndex}",
        )

        while (left < right) {
            tracer.read()
            tracer.read()
            tracer.comparison()
            val sum = numbers[left] + numbers[right]
            tracer.step(
                array = numbers,
                pointers = mapOf("left" to left, "right" to right),
                highlighted = setOf(left, right),
                narration = "${numbers[left]} + ${numbers[right]} = $sum, target $target",
            )

            when {
                sum == target -> {
                    tracer.step(
                        array = numbers,
                        pointers = mapOf("left" to left, "right" to right),
                        highlighted = setOf(left, right),
                        narration = "Found the pair at indices $left and $right",
                    )
                    return intArrayOf(left, right)
                }

                sum < target -> {
                    left++
                    tracer.step(
                        array = numbers,
                        pointers = mapOf("left" to left, "right" to right),
                        narration = "Sum < target, move left to $left",
                    )
                }

                else -> {
                    right--
                    tracer.step(
                        array = numbers,
                        pointers = mapOf("left" to left, "right" to right),
                        narration = "Sum > target, move right to $right",
                    )
                }
            }
        }

        tracer.step(array = numbers, narration = "No pair adds up to $target")
        return intArrayOf(-1, -1)
    }
}
