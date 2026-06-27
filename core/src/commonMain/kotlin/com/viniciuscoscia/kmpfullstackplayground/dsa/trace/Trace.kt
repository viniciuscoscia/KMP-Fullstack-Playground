package com.viniciuscoscia.kmpfullstackplayground.dsa.trace

/**
 * Running counters captured alongside each [AlgoStep], so a visualization can show
 * "how much work" an algorithm has done up to a given moment.
 */
data class Metrics(
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val reads: Int = 0,
)

/**
 * A single observable moment in an algorithm's execution — the snapshot a renderer draws.
 *
 * @param array the data being operated on at this step
 * @param pointers named indices to highlight (e.g. "left" -> 0, "right" -> 5)
 * @param highlighted indices under active comparison/operation
 * @param narration human-readable description of what just happened
 * @param metrics running counters up to and including this step
 */
data class AlgoStep(
    val array: List<Int>,
    val pointers: Map<String, Int> = emptyMap(),
    val highlighted: Set<Int> = emptySet(),
    val narration: String = "",
    val metrics: Metrics = Metrics(),
)

/**
 * An ordered, replayable sequence of [AlgoStep]s produced by an instrumented algorithm run.
 * Pure data: the presentation layer animates between consecutive steps.
 */
data class Trace(val steps: List<AlgoStep>) {
    val size: Int get() = steps.size
    val isEmpty: Boolean get() = steps.isEmpty()

    operator fun get(index: Int): AlgoStep = steps[index]

    companion object {
        val EMPTY = Trace(emptyList())
    }
}
