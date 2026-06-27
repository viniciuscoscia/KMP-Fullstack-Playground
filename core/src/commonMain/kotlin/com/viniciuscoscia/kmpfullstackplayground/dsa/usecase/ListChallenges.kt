package com.viniciuscoscia.kmpfullstackplayground.dsa.usecase

import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.Challenge
import com.viniciuscoscia.kmpfullstackplayground.dsa.domain.ChallengeRepository

/** Lists every challenge in the catalog (for the gallery/list screen). */
class ListChallenges(private val repository: ChallengeRepository) {
    operator fun invoke(): List<Challenge> = repository.all()
}
