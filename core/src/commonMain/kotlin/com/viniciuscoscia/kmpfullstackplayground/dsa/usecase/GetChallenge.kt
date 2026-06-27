package com.viniciuscoscia.kmpfullstackplayground.dsa.usecase

import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.Challenge
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeId
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeRepository

/** Fetches a single challenge by id, or null if it isn't in the catalog. */
class GetChallenge(private val repository: ChallengeRepository) {
    operator fun invoke(id: ChallengeId): Challenge? = repository.byId(id)
}
