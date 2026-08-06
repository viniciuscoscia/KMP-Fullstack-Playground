package com.viniciuscoscia.kmpfullstackplayground.assistant

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogApplicationService
import com.viniciuscoscia.kmpfullstackplayground.research.ResearchApplicationService
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceClaim
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.PageResponse
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJob
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SubstanceSummary
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SubstanceAtlasMcpTools(
    private val catalog: CatalogApplicationService,
    private val research: ResearchApplicationService,
) {
    @McpTool(
        name = "search_substances",
        description = "Search the reviewed substance catalog by name. Scores are evidence summaries, not medical advice.",
        generateOutputSchema = true,
    )
    fun searchSubstances(
        @McpToolParam(description = "Name fragment; use an empty string to browse") query: String,
        @McpToolParam(description = "Zero-based result page", required = false) page: Int? = 0,
        @McpToolParam(description = "Page size from 1 to 100", required = false) size: Int? = 20,
        @McpToolParam(description = "Response locale: pt-BR or en", required = false) locale: String? = "pt-BR",
    ): PageResponse<SubstanceSummary> = catalog.listSubstances(
        query = query,
        tagSlugs = emptyList(),
        page = requirePage(page),
        size = requirePageSize(size),
        locale = requireLocale(locale),
    )

    @McpTool(
        name = "get_substance",
        description = "Get a substance with indications, risks, evidence, regulatory status, and related products.",
        generateOutputSchema = true,
    )
    fun getSubstance(
        @McpToolParam(description = "Substance UUID") substanceId: String,
        @McpToolParam(description = "Response locale: pt-BR or en", required = false) locale: String? = "pt-BR",
    ): Substance = catalog.substance(UUID.fromString(substanceId), requireLocale(locale))

    @McpTool(
        name = "compare_substances",
        description = "Compare two to five substances for one indication. Missing scores mean insufficient evidence.",
        generateOutputSchema = true,
    )
    fun compareSubstances(
        @McpToolParam(description = "Two to five substance UUIDs") substanceIds: List<String>,
        @McpToolParam(description = "Indication UUID used to scope efficacy") indicationId: String,
        @McpToolParam(description = "Response locale: pt-BR or en", required = false) locale: String? = "pt-BR",
    ): List<Substance> = catalog.compare(
        ids = substanceIds.map(UUID::fromString),
        indicationId = UUID.fromString(indicationId),
        locale = requireLocale(locale),
    )

    @McpTool(
        name = "list_evidence",
        description = "List evidence claims and source identifiers for a substance.",
        generateOutputSchema = true,
    )
    fun listEvidence(
        @McpToolParam(description = "Substance UUID") substanceId: String,
        @McpToolParam(description = "Response locale: pt-BR or en", required = false) locale: String? = "pt-BR",
    ): List<EvidenceClaim> = catalog.substance(UUID.fromString(substanceId), requireLocale(locale)).evidence

    @McpTool(
        name = "get_research_status",
        description = "Get the persisted state and item-level outcomes for a research job.",
        generateOutputSchema = true,
    )
    fun getResearchStatus(
        @McpToolParam(description = "Research job UUID") researchJobId: String,
    ): ResearchJob = research.job(UUID.fromString(researchJobId))

    @McpTool(
        name = "request_substance_research",
        description = "Queue one to 100 substance names for evidence collection. Results remain drafts until human approval.",
        generateOutputSchema = true,
    )
    fun requestSubstanceResearch(
        @McpToolParam(description = "One to 100 substance names") terms: List<String>,
        @McpToolParam(description = "Unique key for safe retries, 8 to 120 characters") idempotencyKey: String,
        @McpToolParam(description = "Research locale: pt-BR or en", required = false) locale: String? = "pt-BR",
    ): ResearchJob = research.createJob(terms, requireLocale(locale), idempotencyKey)

    private fun requirePage(page: Int?): Int = requireNotNull(page) { "page is required when explicitly provided" }
        .also { require(it >= 0) { "page must be zero or greater" } }

    private fun requirePageSize(size: Int?): Int = requireNotNull(size) { "size is required when explicitly provided" }
        .also { require(it in 1..100) { "size must be between 1 and 100" } }

    private fun requireLocale(locale: String?): String = requireNotNull(locale) {
        "locale is required when explicitly provided"
    }.also { require(it == "pt-BR" || it == "en") { "locale must be pt-BR or en" } }
}
