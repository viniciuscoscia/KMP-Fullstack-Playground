package com.viniciuscoscia.kmpfullstackplayground.research.persistence

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchItemStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "research_job")
class ResearchJobEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    var idempotencyKey: String = "",
    @Column(nullable = false, length = 16)
    var locale: String = "pt-BR",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: ResearchStatus = ResearchStatus.QUEUED,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
    @Version
    @Column(name = "entity_version", nullable = false)
    var entityVersion: Long = 0,
) {
    @OneToMany(mappedBy = "job", cascade = [CascadeType.ALL], orphanRemoval = true)
    var items: MutableSet<ResearchJobItemEntity> = linkedSetOf()

    @PreUpdate
    fun touch() {
        updatedAt = Instant.now()
    }
}

@Entity
@Table(name = "research_job_item")
class ResearchJobItemEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "research_job_id", nullable = false)
    var job: ResearchJobEntity = ResearchJobEntity(),
    @Column(name = "requested_term", nullable = false, length = 160)
    var requestedTerm: String = "",
    @Column(name = "normalized_term", length = 200)
    var normalizedTerm: String? = null,
    @Column(name = "substance_id")
    var substanceId: UUID? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: ResearchItemStatus = ResearchItemStatus.QUEUED,
    @Column(nullable = false)
    var attempts: Int = 0,
    @Column(name = "error_code", length = 80)
    var errorCode: String? = null,
    @Column(name = "error_message", length = 1000)
    var errorMessage: String? = null,
    @Column(name = "next_retry_at")
    var nextRetryAt: Instant? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun touch() {
        updatedAt = Instant.now()
    }
}

@Entity
@Table(name = "draft_revision")
class DraftRevisionEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "substance_id", nullable = false)
    var substanceId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_job_item_id")
    var researchJobItem: ResearchJobItemEntity? = null,
    @Column(nullable = false)
    var revision: Int = 1,
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 32)
    var reviewStatus: ReviewStatus = ReviewStatus.DRAFT,
    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    var payloadJson: String = "{}",
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "review_decision")
class ReviewDecisionEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_revision_id", nullable = false, unique = true)
    var draftRevision: DraftRevisionEntity = DraftRevisionEntity(),
    @Column(nullable = false, length = 32)
    var decision: String = "",
    @Column(nullable = false, length = 160)
    var reviewer: String = "local-admin",
    @Column(nullable = false, length = 500)
    var reason: String = "",
    @Column(name = "decided_at", nullable = false)
    var decidedAt: Instant = Instant.now(),
)
