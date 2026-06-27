package com.viniciuscoscia.kmpfullstackplayground.dsa.usecase

import com.viniciuscoscia.kmpfullstackplayground.dsa.algorithm.TwoSumSorted
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeId
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeRepository
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.Technique
import com.viniciuscoscia.kmpfullstackplayground.dsa.trace.RecordingTracer
import com.viniciuscoscia.kmpfullstackplayground.dsa.trace.Trace

/**
 * Produces an animation [Trace] for a challenge by running its algorithm with a [RecordingTracer].
 *
 * This is the single capability that every adapter shares — the Compose UI, the Spring REST
 * endpoint, and the Spring AI MCP tool all call it.
 *
 * Slice note: the technique -> algorithm mapping is a simple `when` for now; as more algorithms
 * land this graduates to a registry keyed by challenge id.
 */
class GenerateTrace(private val repository: ChallengeRepository) {
    operator fun invoke(id: ChallengeId): Trace {
        val challenge = repository.byId(id) ?: return Trace.EMPTY
        val tracer = RecordingTracer()
        when (challenge.technique) {
            Technique.TWO_POINTERS ->
                TwoSumSorted().solve(challenge.sampleInput, challenge.sampleTarget, tracer)
            else -> Unit // not yet visualized
        }
        return tracer.build()
    }
}
