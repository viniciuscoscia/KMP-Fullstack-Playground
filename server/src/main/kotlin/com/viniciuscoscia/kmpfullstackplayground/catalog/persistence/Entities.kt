package com.viniciuscoscia.kmpfullstackplayground.catalog.persistence

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceLevel
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ParseStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.TagGroup
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "brand")
class BrandEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true, length = 160)
    var name: String = "",
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @Version
    @Column(name = "entity_version", nullable = false)
    var entityVersion: Long = 0,
) {
    @PreUpdate
    fun touch() {
        updatedAt = Instant.now()
    }
}

@Entity
@Table(name = "substance")
class SubstanceEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "canonical_name", nullable = false, unique = true, length = 200)
    var canonicalName: String = "",
    @Column(name = "description_pt", nullable = false, length = 2000)
    var descriptionPt: String = "",
    @Column(name = "description_en", nullable = false, length = 2000)
    var descriptionEn: String = "",
    @Column(name = "efficacy_summary_score")
    var efficacySummaryScore: Int? = null,
    @Column(name = "risk_overall_score")
    var riskOverallScore: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_level", nullable = false, length = 32)
    var evidenceLevel: EvidenceLevel = EvidenceLevel.INSUFFICIENT,
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 32)
    var reviewStatus: ReviewStatus = ReviewStatus.DRAFT,
    @Column(name = "published_revision", nullable = false)
    var publishedRevision: Int = 0,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @Version
    @Column(name = "entity_version", nullable = false)
    var entityVersion: Long = 0,
) {
    @OneToMany(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var aliases: MutableSet<SubstanceAliasEntity> = linkedSetOf()

    @ManyToMany
    @JoinTable(
        name = "substance_tag",
        joinColumns = [JoinColumn(name = "substance_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")],
    )
    var tags: MutableSet<TagEntity> = linkedSetOf()

    @OneToMany(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var indications: MutableSet<EfficacyAssessmentEntity> = linkedSetOf()

    @OneToOne(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var riskProfile: RiskProfileEntity? = null

    @OneToMany(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var evidenceClaims: MutableSet<EvidenceClaimEntity> = linkedSetOf()

    @OneToMany(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var adverseEffects: MutableSet<AdverseEffectEntity> = linkedSetOf()

    @OneToMany(mappedBy = "substance", cascade = [CascadeType.ALL], orphanRemoval = true)
    var regulatoryStatuses: MutableSet<RegulatoryStatusEntity> = linkedSetOf()

    @ManyToMany(mappedBy = "substances")
    var products: MutableSet<ProductEntity> = linkedSetOf()

    @PreUpdate
    fun touch() {
        updatedAt = Instant.now()
    }
}

@Entity
@Table(name = "substance_alias")
class SubstanceAliasEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    var substance: SubstanceEntity = SubstanceEntity(),
    @Column(nullable = false, length = 16)
    var locale: String = "en",
    @Column(name = "alias", nullable = false, length = 200)
    var value: String = "",
    @Column(name = "normalization_source", nullable = false, length = 120)
    var normalizationSource: String = "manual",
)

@Entity
@Table(name = "tag")
class TagEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true, length = 120)
    var slug: String = "",
    @Column(name = "label_pt", nullable = false, length = 160)
    var labelPt: String = "",
    @Column(name = "label_en", nullable = false, length = 160)
    var labelEn: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "tag_group", nullable = false, length = 48)
    var group: TagGroup = TagGroup.EVIDENCE_STATUS,
)

@Entity
@Table(name = "product")
class ProductEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    var brand: BrandEntity? = null,
    @Column(name = "original_name", nullable = false, length = 500)
    var originalName: String = "",
    @Column(name = "store_category", length = 160)
    var storeCategory: String? = null,
    @Column(name = "observed_price", precision = 12, scale = 2)
    var observedPrice: BigDecimal? = null,
    @Column(length = 3)
    var currency: String? = null,
    @Column(name = "listing_url", length = 1000)
    var listingUrl: String? = null,
    @Column(name = "captured_at", nullable = false)
    var capturedAt: Instant = Instant.now(),
    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 32)
    var parseStatus: ParseStatus = ParseStatus.NEEDS_INPUT,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @Version
    @Column(name = "entity_version", nullable = false)
    var entityVersion: Long = 0,
) {
    @ManyToMany
    @JoinTable(
        name = "product_substance",
        joinColumns = [JoinColumn(name = "product_id")],
        inverseJoinColumns = [JoinColumn(name = "substance_id")],
    )
    var substances: MutableSet<SubstanceEntity> = linkedSetOf()

    @PreUpdate
    fun touch() {
        updatedAt = Instant.now()
    }
}

@Entity
@Table(name = "indication")
class IndicationEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true, length = 160)
    var slug: String = "",
    @Column(name = "label_pt", nullable = false, length = 240)
    var labelPt: String = "",
    @Column(name = "label_en", nullable = false, length = 240)
    var labelEn: String = "",
)

@Entity
@Table(name = "efficacy_assessment")
class EfficacyAssessmentEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    var substance: SubstanceEntity = SubstanceEntity(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "indication_id", nullable = false)
    var indication: IndicationEntity = IndicationEntity(),
    @Column(nullable = false, length = 500)
    var population: String = "",
    @Column(nullable = false, length = 500)
    var outcome: String = "",
    @Column(name = "efficacy_score")
    var efficacyScore: Int? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_level", nullable = false, length = 32)
    var evidenceLevel: EvidenceLevel = EvidenceLevel.INSUFFICIENT,
    @Column(name = "rationale_pt", nullable = false, length = 3000)
    var rationalePt: String = "",
    @Column(name = "rationale_en", nullable = false, length = 3000)
    var rationaleEn: String = "",
    @Column(name = "rubric_version", nullable = false, length = 40)
    var rubricVersion: String = "1.0",
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 32)
    var reviewStatus: ReviewStatus = ReviewStatus.DRAFT,
    @Column(name = "publication_revision", nullable = false)
    var publicationRevision: Int = 0,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "risk_profile")
class RiskProfileEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false, unique = true)
    var substance: SubstanceEntity = SubstanceEntity(),
    @Column(name = "context_pt", nullable = false, length = 1000)
    var contextPt: String = "",
    @Column(name = "context_en", nullable = false, length = 1000)
    var contextEn: String = "",
    @Column(name = "overall_score")
    var overallScore: Int? = null,
    @Column(name = "common_burden")
    var commonBurden: Int? = null,
    @Column(name = "severe_acute")
    var severeAcute: Int? = null,
    @Column(name = "chronic_organ")
    var chronicOrgan: Int? = null,
    @Column(name = "dependency_score")
    var dependency: Int? = null,
    @Column(name = "interaction_score")
    var interaction: Int? = null,
    @Column(name = "product_quality")
    var productQuality: Int? = null,
    @Column(name = "regulatory_uncertainty")
    var regulatoryUncertainty: Int? = null,
    @Column(name = "rationale_pt", nullable = false, length = 3000)
    var rationalePt: String = "",
    @Column(name = "rationale_en", nullable = false, length = 3000)
    var rationaleEn: String = "",
    @Column(name = "rubric_version", nullable = false, length = 40)
    var rubricVersion: String = "1.0",
)

@Entity
@Table(name = "source")
class SourceEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, length = 1000)
    var title: String = "",
    @Column(name = "source_type", nullable = false, length = 80)
    var sourceType: String = "",
    @Column(nullable = false, unique = true, length = 1500)
    var url: String = "",
    @Column(length = 120)
    var jurisdiction: String? = null,
    @Column(name = "published_at")
    var publishedAt: Instant? = null,
    @Column(name = "fetched_at", nullable = false)
    var fetchedAt: Instant = Instant.now(),
    @Column(name = "content_hash", nullable = false, length = 128)
    var contentHash: String = "",
)

@Entity
@Table(name = "evidence_claim")
class EvidenceClaimEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    var substance: SubstanceEntity = SubstanceEntity(),
    @Column(name = "claim_pt", nullable = false, length = 3000)
    var claimPt: String = "",
    @Column(name = "claim_en", nullable = false, length = 3000)
    var claimEn: String = "",
    @Column(name = "extract_text", length = 2000)
    var extract: String? = null,
    @Column(name = "publication_revision", nullable = false)
    var publicationRevision: Int = 0,
) {
    @ManyToMany
    @JoinTable(
        name = "evidence_claim_source",
        joinColumns = [JoinColumn(name = "evidence_claim_id")],
        inverseJoinColumns = [JoinColumn(name = "source_id")],
    )
    var sources: MutableSet<SourceEntity> = linkedSetOf()
}

@Entity
@Table(name = "adverse_effect")
class AdverseEffectEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    var substance: SubstanceEntity = SubstanceEntity(),
    @Column(name = "name_pt", nullable = false, length = 300)
    var namePt: String = "",
    @Column(name = "name_en", nullable = false, length = 300)
    var nameEn: String = "",
    @Column(name = "frequency_class", length = 80)
    var frequency: String? = null,
    @Column(name = "severity_class", nullable = false, length = 80)
    var severity: String = "UNKNOWN",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_claim_id")
    var evidenceClaim: EvidenceClaimEntity? = null,
)

@Entity
@Table(name = "regulatory_status")
class RegulatoryStatusEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substance_id", nullable = false)
    var substance: SubstanceEntity = SubstanceEntity(),
    @Column(nullable = false, length = 120)
    var jurisdiction: String = "",
    @Column(nullable = false, length = 160)
    var authority: String = "",
    @Column(name = "status_pt", nullable = false, length = 500)
    var statusPt: String = "",
    @Column(name = "status_en", nullable = false, length = 500)
    var statusEn: String = "",
    @Column(name = "effective_date")
    var effectiveDate: LocalDate? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    var source: SourceEntity = SourceEntity(),
)
