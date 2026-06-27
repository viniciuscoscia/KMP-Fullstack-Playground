package com.viniciuscoscia.kmpfullstackplayground.dsa.trace

/**
 * Port an algorithm calls to emit observable steps.
 *
 * The canonical (interview-style) run uses [NoOpTracer] and pays nothing, so the solution
 * stays clean and allocation-free. Visualization passes a [RecordingTracer] to capture a
 * [Trace] that the UI — and tests — can replay and assert on.
 */
interface Tracer {
    fun step(
        array: List<Int>,
        pointers: Map<String, Int> = emptyMap(),
        highlighted: Set<Int> = emptySet(),
        narration: String = "",
    )

    fun comparison()
    fun swap()
    fun read()
}

/** Default tracer: records nothing. Lets the canonical run ignore instrumentation entirely. */
object NoOpTracer : Tracer {
    override fun step(
        array: List<Int>,
        pointers: Map<String, Int>,
        highlighted: Set<Int>,
        narration: String,
    ) = Unit

    override fun comparison() = Unit
    override fun swap() = Unit
    override fun read() = Unit
}

/** Captures every [step] into an ordered [Trace], snapshotting the running [Metrics]. */
class RecordingTracer : Tracer {
    private val recorded = mutableListOf<AlgoStep>()
    private var comparisons = 0
    private var swaps = 0
    private var reads = 0

    override fun comparison() {
        comparisons++
    }

    override fun swap() {
        swaps++
    }

    override fun read() {
        reads++
    }

    override fun step(
        array: List<Int>,
        pointers: Map<String, Int>,
        highlighted: Set<Int>,
        narration: String,
    ) {
        recorded += AlgoStep(
            array = array,
            pointers = pointers,
            highlighted = highlighted,
            narration = narration,
            metrics = Metrics(comparisons, swaps, reads),
        )
    }

    fun build(): Trace = Trace(recorded.toList())
}
