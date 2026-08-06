package com.viniciuscoscia.kmpfullstackplayground.research

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogPublicationService
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.DraftRevisionEntity
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.DraftRevisionJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ResearchJobEntity
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ResearchJobItemEntity
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ResearchJobItemJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ResearchJobJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ReviewDecisionEntity
import com.viniciuscoscia.kmpfullstackplayground.research.persistence.ReviewDecisionJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.DraftRevision
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchItemStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJob
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJobItem
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewStatus
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SourceRef
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ResearchNotFoundException(resource: String, id: UUID) :
    NoSuchElementException("$resource $id was not found")

data class ClaimedResearchItem(
    val id: UUID,
    val term: String,
    val locale: String,
    val attempts: Int,
)

@Service
class ResearchApplicationService(
    private val jobs: ResearchJobJpaRepository,
    private val items: ResearchJobItemJpaRepository,
    private val drafts: DraftRevisionJpaRepository,
    private val decisions: ReviewDecisionJpaRepository,
    private val catalog: CatalogPublicationService,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    @Transactional
    fun createJob(terms: List<String>, locale: String, idempotencyKey: String): ResearchJob {
        require(idempotencyKey.length in 8..120) { "Idempotency-Key must contain between 8 and 120 characters" }
        require(locale == "pt-BR" || locale == "en") { "locale must be pt-BR or en" }
        val normalizedTerms = terms.map(String::trim)
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)
        require(normalizedTerms.size in 1..100) { "Research jobs require between 1 and 100 unique terms" }

        jobs.findByIdempotencyKey(idempotencyKey)?.let { return it.toContract() }

        val now = Instant.now()
        val job = ResearchJobEntity(
            idempotencyKey = idempotencyKey,
            locale = locale,
            status = ResearchStatus.QUEUED,
            createdAt = now,
            updatedAt = now,
        )
        normalizedTerms.forEach { term ->
            job.items += ResearchJobItemEntity(
                job = job,
                requestedTerm = term,
                status = ResearchItemStatus.QUEUED,
                createdAt = now,
                updatedAt = now,
            )
        }
        return jobs.save(job).toContract()
    }

    @Transactional(readOnly = true)
    fun job(id: UUID): ResearchJob = jobs.findDetailedById(id)?.toContract()
        ?: throw ResearchNotFoundException("Research job", id)

    @Transactional(readOnly = true)
    fun recentJobs(): List<ResearchJob> = jobs.findTop100ByOrderByCreatedAtDesc().map { it.toContract() }

    @Transactional(readOnly = true)
    fun draft(id: UUID): DraftRevision = drafts.findById(id)
        .orElseThrow { ResearchNotFoundException("Draft", id) }
        .toContract(json)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun claimNext(): ClaimedResearchItem? {
        val item = items.lockNext(ResearchItemStatus.QUEUED, Instant.now(), PageRequest.of(0, 1)).firstOrNull()
            ?: return null
        item.status = ResearchItemStatus.RUNNING
        item.attempts += 1
        item.errorCode = null
        item.errorMessage = null
        item.nextRetryAt = null
        item.job.status = ResearchStatus.RUNNING
        return ClaimedResearchItem(item.id, item.requestedTerm, item.job.locale, item.attempts)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun completeResearch(claim: ClaimedResearchItem, result: SourceResearchResult) {
        val item = items.findById(claim.id).orElseThrow { ResearchNotFoundException("Research item", claim.id) }
        val normalized = normalizeTerm(item.requestedTerm)
        if (isAmbiguous(normalized)) {
            item.normalizedTerm = normalized
            item.status = ResearchItemStatus.NEEDS_INPUT
            item.errorCode = "AMBIGUOUS_TERM"
            item.errorMessage = "The term requires manual normalization before research can continue"
            updateJobStatus(item.job)
            return
        }

        val substanceId = catalog.resolveOrCreateDraft(normalized)
        val base = catalog.draftRepresentation(substanceId, item.job.locale)
        val candidate = base.copy(
            sources = result.findings.map { finding ->
                SourceRef(
                    id = UUID.randomUUID().toString(),
                    title = finding.title,
                    sourceType = finding.sourceType,
                    url = finding.url,
                    jurisdiction = finding.jurisdiction,
                    fetchedAt = finding.fetchedAt.toString(),
                    contentHash = finding.contentHash,
                )
            },
        )
        val revision = drafts.maxRevision(substanceId) + 1
        drafts.save(
            DraftRevisionEntity(
                substanceId = substanceId,
                researchJobItem = item,
                revision = revision,
                reviewStatus = ReviewStatus.DRAFT,
                payloadJson = json.encodeToString(candidate),
            ),
        )
        item.normalizedTerm = normalized
        item.substanceId = substanceId
        item.status = ResearchItemStatus.DRAFT_READY
        if (result.failures.isNotEmpty()) {
            item.errorCode = "SOURCE_PARTIAL"
            item.errorMessage = "${result.failures.size} official API sources need retry or manual review"
        }
        updateJobStatus(item.job)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun retryOrFail(claim: ClaimedResearchItem, failures: List<String>) {
        val item = items.findById(claim.id).orElseThrow { ResearchNotFoundException("Research item", claim.id) }
        item.errorCode = "SOURCE_UNAVAILABLE"
        item.errorMessage = failures.take(5).joinToString("; ").take(1000)
        if (item.attempts >= MAX_ATTEMPTS) {
            item.status = ResearchItemStatus.FAILED
            item.nextRetryAt = null
        } else {
            item.status = ResearchItemStatus.QUEUED
            item.nextRetryAt = Instant.now().plus(retryDelaySeconds(item.attempts), ChronoUnit.SECONDS)
        }
        updateJobStatus(item.job)
    }

    @Transactional
    fun approve(id: UUID, reason: String): DraftRevision {
        val draft = drafts.findById(id).orElseThrow { ResearchNotFoundException("Draft", id) }
        require(draft.reviewStatus == ReviewStatus.DRAFT || draft.reviewStatus == ReviewStatus.IN_REVIEW) {
            "Only an open draft can be approved"
        }
        val candidate = json.decodeFromString<Substance>(draft.payloadJson)
        val published = catalog.publish(candidate)
        draft.reviewStatus = ReviewStatus.APPROVED
        draft.researchJobItem?.status = ResearchItemStatus.COMPLETED
        decisions.save(decision(draft, "APPROVED", reason))
        draft.researchJobItem?.job?.let(::updateJobStatus)
        return draft.toContract(json).copy(summary = published)
    }

    @Transactional
    fun reject(id: UUID, reason: String): DraftRevision {
        val draft = drafts.findById(id).orElseThrow { ResearchNotFoundException("Draft", id) }
        require(draft.reviewStatus == ReviewStatus.DRAFT || draft.reviewStatus == ReviewStatus.IN_REVIEW) {
            "Only an open draft can be rejected"
        }
        draft.reviewStatus = ReviewStatus.REJECTED
        draft.researchJobItem?.status = ResearchItemStatus.COMPLETED
        decisions.save(decision(draft, "REJECTED", reason))
        draft.researchJobItem?.job?.let(::updateJobStatus)
        return draft.toContract(json)
    }

    private fun decision(draft: DraftRevisionEntity, value: String, reason: String): ReviewDecisionEntity {
        require(reason.trim().length in 3..500) { "Review reason must contain between 3 and 500 characters" }
        val reviewer = SecurityContextHolder.getContext().authentication?.name ?: "local-admin"
        return ReviewDecisionEntity(
            draftRevision = draft,
            decision = value,
            reviewer = reviewer.take(160),
            reason = reason.trim(),
        )
    }

    private fun updateJobStatus(job: ResearchJobEntity) {
        val statuses = job.items.map { it.status }
        job.status = when {
            statuses.all { it == ResearchItemStatus.FAILED } -> ResearchStatus.FAILED
            statuses.any { it == ResearchItemStatus.RUNNING || it == ResearchItemStatus.QUEUED } -> ResearchStatus.RUNNING
            statuses.any { it == ResearchItemStatus.NEEDS_INPUT } -> ResearchStatus.NEEDS_INPUT
            statuses.any { it == ResearchItemStatus.FAILED } -> ResearchStatus.PARTIAL
            statuses.all { it == ResearchItemStatus.DRAFT_READY || it == ResearchItemStatus.COMPLETED } -> ResearchStatus.COMPLETED
            else -> ResearchStatus.PARTIAL
        }
        job.updatedAt = Instant.now()
    }

    private fun normalizeTerm(term: String): String = term.trim().replace(Regex("\\s+"), " ")

    private fun isAmbiguous(term: String): Boolean =
        term.count { it == '(' } != term.count { it == ')' } || term.contains("unknown", ignoreCase = true)

    private fun retryDelaySeconds(attempt: Int): Long = when (attempt) {
        1 -> 5
        2 -> 15
        else -> 45
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}

@Component
class ResearchJobWorker(
    private val service: ResearchApplicationService,
    private val sourceAdapter: EvidenceSourceAdapter,
) {
    @Scheduled(fixedDelayString = "\${substance-atlas.research.fixed-delay:3000}")
    fun processOne() {
        val claim = service.claimNext() ?: return
        runCatching { sourceAdapter.research(claim.term) }
            .onSuccess { result ->
                val apiFinding = result.findings.any { !it.requiresManualReview }
                if (result.failures.isNotEmpty() && !apiFinding) {
                    service.retryOrFail(claim, result.failures)
                } else {
                    service.completeResearch(claim, result)
                }
            }
            .onFailure { error -> service.retryOrFail(claim, listOf(error.javaClass.simpleName)) }
    }
}

private fun ResearchJobEntity.toContract(): ResearchJob = ResearchJob(
    id = id.toString(),
    status = status,
    items = items.sortedBy { it.createdAt }.map { item ->
        ResearchJobItem(
            id = item.id.toString(),
            requestedTerm = item.requestedTerm,
            normalizedTerm = item.normalizedTerm,
            substanceId = item.substanceId?.toString(),
            status = item.status,
            attempts = item.attempts,
            errorCode = item.errorCode,
            errorMessage = item.errorMessage,
        )
    },
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

private fun DraftRevisionEntity.toContract(json: Json): DraftRevision = DraftRevision(
    id = id.toString(),
    substanceId = substanceId.toString(),
    revision = revision,
    status = reviewStatus,
    summary = json.decodeFromString(payloadJson),
    createdAt = createdAt.toString(),
)
