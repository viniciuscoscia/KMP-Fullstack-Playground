package com.viniciuscoscia.kmpfullstackplayground.substance.contract

import kotlinx.serialization.Serializable

@Serializable
enum class EvidenceLevel { HIGH, MODERATE, LOW, VERY_LOW, INSUFFICIENT }

@Serializable
enum class ReviewStatus { DRAFT, IN_REVIEW, APPROVED, REJECTED }

@Serializable
enum class ResearchStatus { QUEUED, RUNNING, PARTIAL, NEEDS_INPUT, COMPLETED, FAILED }

@Serializable
enum class ResearchItemStatus { QUEUED, RUNNING, NEEDS_INPUT, DRAFT_READY, COMPLETED, FAILED }

@Serializable
enum class TagGroup {
    THERAPEUTIC_CLASS,
    MECHANISM,
    PHYSIOLOGICAL_EFFECT,
    CLINICAL_USE,
    SPORT_CONTEXT,
    RISK,
    REGULATORY_STATUS,
    EVIDENCE_STATUS,
}

@Serializable
enum class ParseStatus { PARSED, NEEDS_INPUT, DUPLICATE, REJECTED }

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val total: Long,
) {
    init {
        require(page >= 0) { "page must be non-negative" }
        require(size in 1..100) { "size must be between 1 and 100" }
        require(total >= 0) { "total must be non-negative" }
    }
}

@Serializable
data class Dashboard(
    val productCount: Long,
    val substanceCount: Long,
    val sourceCoveragePercent: Double,
    val highRiskCount: Long,
    val reviewQueueCount: Long,
    val evidenceDistribution: Map<EvidenceLevel, Long> = emptyMap(),
    val researchDistribution: Map<ResearchStatus, Long> = emptyMap(),
)

@Serializable
data class Tag(
    val id: String,
    val slug: String,
    val label: String,
    val group: TagGroup,
)

@Serializable
data class SubstanceSummary(
    val id: String,
    val name: String,
    val description: String,
    val tags: List<Tag> = emptyList(),
    val efficacyScore: Int? = null,
    val riskScore: Int? = null,
    val evidenceLevel: EvidenceLevel = EvidenceLevel.INSUFFICIENT,
    val reviewStatus: ReviewStatus = ReviewStatus.DRAFT,
) {
    init {
        validateScore(efficacyScore, "efficacyScore")
        validateScore(riskScore, "riskScore")
    }
}

@Serializable
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val storeCategory: String? = null,
    val observedPrice: Double? = null,
    val currency: String? = null,
    val listingUrl: String? = null,
    val capturedAt: String,
    val parseStatus: ParseStatus,
    val substances: List<SubstanceSummary> = emptyList(),
)

@Serializable
data class IndicationAssessment(
    val id: String,
    val indicationId: String,
    val indication: String,
    val population: String,
    val outcome: String,
    val efficacyScore: Int? = null,
    val evidenceLevel: EvidenceLevel,
    val rationale: String,
    val rubricVersion: String,
    val reviewStatus: ReviewStatus,
) {
    init {
        validateScore(efficacyScore, "efficacyScore")
    }
}

@Serializable
data class RiskProfile(
    val context: String,
    val overallScore: Int? = null,
    val commonBurden: Int? = null,
    val severeAcute: Int? = null,
    val chronicOrgan: Int? = null,
    val dependency: Int? = null,
    val interaction: Int? = null,
    val productQuality: Int? = null,
    val regulatoryUncertainty: Int? = null,
    val rationale: String,
    val rubricVersion: String,
) {
    init {
        listOf(
            "overallScore" to overallScore,
            "commonBurden" to commonBurden,
            "severeAcute" to severeAcute,
            "chronicOrgan" to chronicOrgan,
            "dependency" to dependency,
            "interaction" to interaction,
            "productQuality" to productQuality,
            "regulatoryUncertainty" to regulatoryUncertainty,
        ).forEach { (name, value) -> validateScore(value, name) }
    }
}

@Serializable
data class AdverseEffect(
    val id: String,
    val name: String,
    val frequency: String? = null,
    val severity: String,
    val evidenceClaimId: String? = null,
)

@Serializable
data class RegulatoryStatus(
    val id: String,
    val jurisdiction: String,
    val authority: String,
    val status: String,
    val effectiveDate: String? = null,
    val sourceId: String,
)

@Serializable
data class SourceRef(
    val id: String,
    val title: String,
    val sourceType: String,
    val url: String,
    val jurisdiction: String? = null,
    val publishedAt: String? = null,
    val fetchedAt: String,
    val contentHash: String,
)

@Serializable
data class EvidenceClaim(
    val id: String,
    val claim: String,
    val sourceIds: List<String>,
    val extract: String? = null,
) {
    init {
        require(sourceIds.isNotEmpty()) { "An evidence claim requires at least one source" }
    }
}

@Serializable
data class Substance(
    val summary: SubstanceSummary,
    val aliases: List<String> = emptyList(),
    val indications: List<IndicationAssessment> = emptyList(),
    val riskProfile: RiskProfile? = null,
    val adverseEffects: List<AdverseEffect> = emptyList(),
    val regulatoryStatuses: List<RegulatoryStatus> = emptyList(),
    val evidence: List<EvidenceClaim> = emptyList(),
    val sources: List<SourceRef> = emptyList(),
    val products: List<Product> = emptyList(),
)

@Serializable
data class CreateResearchJobRequest(
    val terms: List<String>,
    val locale: String,
) {
    init {
        require(terms.size in 1..100) { "terms must contain between 1 and 100 entries" }
        require(terms.all { it.trim().length in 2..160 }) { "Each term must contain between 2 and 160 characters" }
        require(locale == "pt-BR" || locale == "en") { "locale must be pt-BR or en" }
    }
}

@Serializable
data class ResearchJobItem(
    val id: String,
    val requestedTerm: String,
    val normalizedTerm: String? = null,
    val substanceId: String? = null,
    val status: ResearchItemStatus,
    val attempts: Int = 0,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)

@Serializable
data class ResearchJob(
    val id: String,
    val status: ResearchStatus,
    val items: List<ResearchJobItem>,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class DraftRevision(
    val id: String,
    val substanceId: String,
    val revision: Int,
    val status: ReviewStatus,
    val summary: Substance,
    val createdAt: String,
)

@Serializable
data class ReviewDecisionRequest(
    val reason: String,
) {
    init {
        require(reason.trim().length in 3..500) { "reason must contain between 3 and 500 characters" }
    }
}

@Serializable
data class Problem(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String? = null,
    val instance: String? = null,
)

private fun validateScore(value: Int?, name: String) {
    require(value == null || value in 0..10) { "$name must be null or between 0 and 10" }
}
