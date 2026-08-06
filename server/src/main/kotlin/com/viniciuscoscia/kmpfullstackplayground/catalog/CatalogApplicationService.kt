package com.viniciuscoscia.kmpfullstackplayground.catalog

import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.ProductEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.ProductJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.SourceEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.SourceJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.SubstanceEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.SubstanceJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.TagEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.TagJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.AdverseEffect
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Dashboard
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceClaim
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceLevel
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.IndicationAssessment
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.PageResponse
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Product
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.RegulatoryStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.RiskProfile
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SourceRef
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SubstanceSummary
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

class CatalogNotFoundException(resource: String, id: UUID) :
    NoSuchElementException("$resource $id was not found")

@Service
class CatalogApplicationService(
    private val substances: SubstanceJpaRepository,
    private val products: ProductJpaRepository,
    private val tags: TagJpaRepository,
    private val sources: SourceJpaRepository,
) {
    @Transactional(readOnly = true)
    fun dashboard(): Dashboard {
        val substanceCount = substances.count()
        val sourceCoverage = if (substanceCount == 0L) {
            0.0
        } else {
            substances.countWithEvidence().toDouble() * 100.0 / substanceCount.toDouble()
        }
        val evidenceDistribution = substances.evidenceDistribution().associate { row ->
            row[0] as EvidenceLevel to row[1] as Long
        }
        return Dashboard(
            productCount = products.count(),
            substanceCount = substanceCount,
            sourceCoveragePercent = sourceCoverage,
            highRiskCount = substances.countByRiskOverallScoreGreaterThanEqual(7),
            reviewQueueCount = substances.countByReviewStatusIn(listOf(ReviewStatus.DRAFT, ReviewStatus.IN_REVIEW)),
            evidenceDistribution = evidenceDistribution,
            researchDistribution = emptyMap<ResearchStatus, Long>(),
        )
    }

    @Transactional(readOnly = true)
    fun listProducts(query: String, page: Int, size: Int, locale: String): PageResponse<Product> {
        val pageable = PageRequest.of(page, size.coerceAtMost(100), Sort.by("originalName").ascending())
        val result = products.findByOriginalNameContainingIgnoreCase(query.trim(), pageable)
        return PageResponse(result.content.map { it.toContract(locale) }, page, result.size, result.totalElements)
    }

    @Transactional(readOnly = true)
    fun product(id: UUID, locale: String): Product =
        products.findById(id).orElseThrow { CatalogNotFoundException("Product", id) }.toContract(locale)

    @Transactional(readOnly = true)
    fun listSubstances(
        query: String,
        tagSlugs: List<String>,
        page: Int,
        size: Int,
        locale: String,
    ): PageResponse<SubstanceSummary> {
        val pageable = PageRequest.of(page, size.coerceAtMost(100), Sort.by("canonicalName").ascending())
        val result = if (tagSlugs.isEmpty()) {
            substances.findByCanonicalNameContainingIgnoreCase(query.trim(), pageable)
        } else {
            substances.searchByNameAndTags(query.trim(), tagSlugs, pageable)
        }
        return PageResponse(result.content.map { it.toSummary(locale) }, page, result.size, result.totalElements)
    }

    @Transactional(readOnly = true)
    fun substance(id: UUID, locale: String, approvedOnly: Boolean = false): Substance {
        val entity = substances.findById(id).orElseThrow { CatalogNotFoundException("Substance", id) }
        if (approvedOnly && entity.reviewStatus != ReviewStatus.APPROVED) {
            throw CatalogNotFoundException("Approved substance", id)
        }
        return entity.toContract(locale)
    }

    @Transactional(readOnly = true)
    fun compare(ids: List<UUID>, indicationId: UUID, locale: String): List<Substance> {
        require(ids.size in 2..5) { "Compare requires between 2 and 5 substances" }
        return ids.map { id ->
            val detail = substance(id, locale)
            detail.copy(indications = detail.indications.filter { it.indicationId == indicationId.toString() })
        }
    }

    @Transactional(readOnly = true)
    fun listTags(locale: String): List<Tag> = tags.findAllByOrderByGroupAscLabelPtAsc().map { it.toContract(locale) }

    @Transactional(readOnly = true)
    fun listSources(page: Int, size: Int): PageResponse<SourceRef> {
        val pageable = PageRequest.of(page, size.coerceAtMost(100), Sort.by("title").ascending())
        val result = sources.findAll(pageable)
        return PageResponse(result.content.map(SourceEntity::toContract), page, result.size, result.totalElements)
    }

    @Transactional(readOnly = true)
    fun exportSnapshot(locale: String): CatalogExportSnapshot = CatalogExportSnapshot(
        products = products.findAll(Sort.by("originalName")).map { it.toContract(locale) },
        substances = substances.findAll(Sort.by("canonicalName")).map { it.toContract(locale) },
        sources = sources.findAll(Sort.by("title")).map(SourceEntity::toContract),
    )
}

@Service
class CatalogPublicationService(
    private val substances: SubstanceJpaRepository,
) {
    @Transactional
    fun resolveOrCreateDraft(term: String): UUID {
        val canonical = term.trim().replace(Regex("\\s+"), " ")
        substances.findByCanonicalNameIgnoreCase(canonical)?.let { return it.id }
        return substances.save(
            SubstanceEntity(
                canonicalName = canonical,
                descriptionPt = "Rascunho de pesquisa aguardando revisão humana.",
                descriptionEn = "Research draft awaiting human review.",
                evidenceLevel = EvidenceLevel.INSUFFICIENT,
                reviewStatus = ReviewStatus.DRAFT,
            ),
        ).id
    }

    @Transactional(readOnly = true)
    fun draftRepresentation(id: UUID, locale: String): Substance {
        val entity = substances.findById(id).orElseThrow { CatalogNotFoundException("Substance", id) }
        return entity.toContract(locale)
    }

    @Transactional
    fun publish(candidate: Substance): Substance {
        val id = UUID.fromString(candidate.summary.id)
        val entity = substances.findById(id).orElseThrow { CatalogNotFoundException("Substance", id) }
        entity.canonicalName = candidate.summary.name
        if (candidate.summary.description.isNotBlank()) {
            entity.descriptionPt = candidate.summary.description
            entity.descriptionEn = candidate.summary.description
        }
        entity.efficacySummaryScore = candidate.summary.efficacyScore
        entity.riskOverallScore = candidate.summary.riskScore
        entity.evidenceLevel = candidate.summary.evidenceLevel
        entity.reviewStatus = ReviewStatus.APPROVED
        entity.publishedRevision += 1
        return substances.save(entity).toContract("en")
    }
}

data class CatalogExportSnapshot(
    val products: List<Product>,
    val substances: List<Substance>,
    val sources: List<SourceRef>,
)

private fun String.isEnglish(): Boolean = lowercase().startsWith("en")

private fun SubstanceEntity.toSummary(locale: String): SubstanceSummary = SubstanceSummary(
    id = id.toString(),
    name = canonicalName,
    description = if (locale.isEnglish()) descriptionEn else descriptionPt,
    tags = tags.sortedBy { it.slug }.map { it.toContract(locale) },
    efficacyScore = efficacySummaryScore,
    riskScore = riskOverallScore,
    evidenceLevel = evidenceLevel,
    reviewStatus = reviewStatus,
)

private fun TagEntity.toContract(locale: String): Tag = Tag(
    id = id.toString(),
    slug = slug,
    label = if (locale.isEnglish()) labelEn else labelPt,
    group = group,
)

private fun ProductEntity.toContract(locale: String): Product = Product(
    id = id.toString(),
    name = originalName,
    brand = brand?.name ?: "Unknown",
    storeCategory = storeCategory,
    observedPrice = observedPrice?.toDouble(),
    currency = currency,
    listingUrl = listingUrl,
    capturedAt = capturedAt.toString(),
    parseStatus = parseStatus,
    substances = substances.sortedBy { it.canonicalName }.map { it.toSummary(locale) },
)

private fun SubstanceEntity.toContract(locale: String): Substance = Substance(
    summary = toSummary(locale),
    aliases = aliases.sortedBy { it.value }.map { it.value },
    indications = indications.sortedBy { it.indication.slug }.map { assessment ->
        IndicationAssessment(
            id = assessment.id.toString(),
            indicationId = assessment.indication.id.toString(),
            indication = if (locale.isEnglish()) assessment.indication.labelEn else assessment.indication.labelPt,
            population = assessment.population,
            outcome = assessment.outcome,
            efficacyScore = assessment.efficacyScore,
            evidenceLevel = assessment.evidenceLevel,
            rationale = if (locale.isEnglish()) assessment.rationaleEn else assessment.rationalePt,
            rubricVersion = assessment.rubricVersion,
            reviewStatus = assessment.reviewStatus,
        )
    },
    riskProfile = riskProfile?.let { profile ->
        RiskProfile(
            context = if (locale.isEnglish()) profile.contextEn else profile.contextPt,
            overallScore = profile.overallScore,
            commonBurden = profile.commonBurden,
            severeAcute = profile.severeAcute,
            chronicOrgan = profile.chronicOrgan,
            dependency = profile.dependency,
            interaction = profile.interaction,
            productQuality = profile.productQuality,
            regulatoryUncertainty = profile.regulatoryUncertainty,
            rationale = if (locale.isEnglish()) profile.rationaleEn else profile.rationalePt,
            rubricVersion = profile.rubricVersion,
        )
    },
    adverseEffects = adverseEffects.sortedBy { it.nameEn }.map { effect ->
        AdverseEffect(
            id = effect.id.toString(),
            name = if (locale.isEnglish()) effect.nameEn else effect.namePt,
            frequency = effect.frequency,
            severity = effect.severity,
            evidenceClaimId = effect.evidenceClaim?.id?.toString(),
        )
    },
    regulatoryStatuses = regulatoryStatuses.sortedBy { it.jurisdiction }.map { status ->
        RegulatoryStatus(
            id = status.id.toString(),
            jurisdiction = status.jurisdiction,
            authority = status.authority,
            status = if (locale.isEnglish()) status.statusEn else status.statusPt,
            effectiveDate = status.effectiveDate?.toString(),
            sourceId = status.source.id.toString(),
        )
    },
    evidence = evidenceClaims.sortedBy { it.id }.map { claim ->
        EvidenceClaim(
            id = claim.id.toString(),
            claim = if (locale.isEnglish()) claim.claimEn else claim.claimPt,
            sourceIds = claim.sources.map { it.id.toString() },
            extract = claim.extract,
        )
    },
    sources = evidenceClaims.flatMap { it.sources }.distinctBy { it.id }.map(SourceEntity::toContract),
    products = products.sortedBy { it.originalName }.map { it.toContract(locale) },
)

private fun SourceEntity.toContract(): SourceRef = SourceRef(
    id = id.toString(),
    title = title,
    sourceType = sourceType,
    url = url,
    jurisdiction = jurisdiction,
    publishedAt = publishedAt?.toString(),
    fetchedAt = fetchedAt.toString(),
    contentHash = contentHash,
)
