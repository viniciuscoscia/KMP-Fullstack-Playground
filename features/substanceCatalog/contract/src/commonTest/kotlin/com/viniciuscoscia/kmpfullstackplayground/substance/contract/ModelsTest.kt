package com.viniciuscoscia.kmpfullstackplayground.substance.contract

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelsTest {
    @Test
    fun unknownScoresRemainNullAfterSerialization() {
        val summary = SubstanceSummary(
            id = "substance-1",
            name = "Synthetic example",
            description = "Repository-safe fixture",
        )

        val decoded = Json.decodeFromString<SubstanceSummary>(Json.encodeToString(summary))

        assertNull(decoded.efficacyScore)
        assertNull(decoded.riskScore)
        assertTrue("\"efficacyScore\":0" !in Json.encodeToString(summary))
    }

    @Test
    fun scoresOutsideRubricAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            SubstanceSummary(
                id = "substance-1",
                name = "Synthetic example",
                description = "Repository-safe fixture",
                riskScore = 11,
            )
        }
    }

    @Test
    fun researchBatchIsBounded() {
        assertFailsWith<IllegalArgumentException> {
            CreateResearchJobRequest(
                terms = List(101) { "synthetic-$it" },
                locale = "en",
            )
        }
    }
}
