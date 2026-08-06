package com.viniciuscoscia.kmpfullstackplayground.research.web

import com.viniciuscoscia.kmpfullstackplayground.research.ResearchApplicationService
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.CreateResearchJobRequest
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewDecisionRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/v1")
class ResearchController(
    private val research: ResearchApplicationService,
) {
    @GetMapping("/research-jobs")
    fun jobs() = research.recentJobs()

    @PostMapping("/research-jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun createJob(
        @RequestHeader("Idempotency-Key") @Size(min = 8, max = 120) idempotencyKey: String,
        @Valid @RequestBody request: CreateResearchJobRequest,
    ) = research.createJob(request.terms, request.locale, idempotencyKey)

    @GetMapping("/research-jobs/{id}")
    fun job(@PathVariable id: UUID) = research.job(id)

    @GetMapping("/drafts/{id}")
    fun draft(@PathVariable id: UUID) = research.draft(id)

    @PostMapping("/drafts/{id}/approve")
    fun approve(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ) = research.approve(id, request.reason)

    @PostMapping("/drafts/{id}/reject")
    fun reject(
        @PathVariable id: UUID,
        @Valid @RequestBody request: ReviewDecisionRequest,
    ) = research.reject(id, request.reason)
}
