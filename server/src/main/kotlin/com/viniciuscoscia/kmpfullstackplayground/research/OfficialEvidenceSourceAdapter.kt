package com.viniciuscoscia.kmpfullstackplayground.research

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.HexFormat

data class ResearchFinding(
    val title: String,
    val sourceType: String,
    val url: String,
    val jurisdiction: String?,
    val fetchedAt: Instant,
    val contentHash: String,
    val requiresManualReview: Boolean,
)

data class SourceResearchResult(
    val findings: List<ResearchFinding>,
    val failures: List<String>,
)

interface EvidenceSourceAdapter {
    fun research(term: String): SourceResearchResult
    fun allowedHosts(): Set<String>
}

@Component
class OfficialEvidenceSourceAdapter(
    @Value("\${substance-atlas.research.live-enabled:false}") private val liveEnabled: Boolean,
    @Value("\${substance-atlas.research.connect-timeout:3s}") connectTimeout: Duration,
    @Value("\${substance-atlas.research.read-timeout:8s}") private val readTimeout: Duration,
    @Value("\${substance-atlas.research.max-response-bytes:1000000}") private val maxResponseBytes: Int,
) : EvidenceSourceAdapter {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(connectTimeout)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    override fun research(term: String): SourceResearchResult {
        val endpoints = endpoints(term)
        val findings = mutableListOf<ResearchFinding>()
        val failures = mutableListOf<String>()
        endpoints.forEach { endpoint ->
            if (endpoint.manualReviewOnly) {
                findings += endpoint.toFinding(contentHash = "manual-review", manual = true)
            } else if (liveEnabled) {
                runCatching { fetch(endpoint) }
                    .onSuccess(findings::add)
                    .onFailure { failures += "${endpoint.name}: ${it.javaClass.simpleName}" }
            }
        }
        return SourceResearchResult(findings, failures)
    }

    override fun allowedHosts(): Set<String> = ALLOWED_HOSTS

    private fun fetch(endpoint: SourceEndpoint): ResearchFinding {
        require(endpoint.uri.host in ALLOWED_HOSTS) { "Research host is not allowlisted" }
        val request = HttpRequest.newBuilder(endpoint.uri)
            .timeout(readTimeout)
            .header("Accept", "application/json")
            .header("User-Agent", "SubstanceAtlas/1.0 local-research")
            .GET()
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
        require(response.statusCode() in 200..299) { "Official source returned ${response.statusCode()}" }
        val bytes = response.body().use { it.readNBytes(maxResponseBytes + 1) }
        require(bytes.size <= maxResponseBytes) { "Official source response exceeded the configured limit" }
        return endpoint.toFinding(
            contentHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),
            manual = false,
        )
    }

    private fun endpoints(term: String): List<SourceEndpoint> {
        val query = encodeQuery(term)
        val path = encodePath(term)
        return listOf(
            SourceEndpoint(
                name = "RxNorm",
                sourceType = "IDENTITY_API",
                jurisdiction = "US",
                uri = URI.create("https://rxnav.nlm.nih.gov/REST/approximateTerm.json?term=$query&maxEntries=5"),
            ),
            SourceEndpoint(
                name = "PubChem",
                sourceType = "IDENTITY_API",
                jurisdiction = null,
                uri = URI.create("https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/$path/property/Title,IUPACName,CanonicalSMILES/JSON"),
            ),
            SourceEndpoint(
                name = "PubMed",
                sourceType = "LITERATURE_INDEX",
                jurisdiction = null,
                uri = URI.create("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=$query&retmax=10&retmode=json"),
            ),
            SourceEndpoint(
                name = "DailyMed",
                sourceType = "REGULATORY_LABEL",
                jurisdiction = "US",
                uri = URI.create("https://dailymed.nlm.nih.gov/dailymed/services/v2/spls.json?drug_name=$query&pagesize=5"),
            ),
            SourceEndpoint(
                name = "openFDA",
                sourceType = "REGULATORY_API",
                jurisdiction = "US",
                uri = URI.create("https://api.fda.gov/drug/label.json?search=${encodeQuery("openfda.generic_name:\"$term\"")}&limit=5"),
            ),
            SourceEndpoint(
                name = "ClinicalTrials.gov",
                sourceType = "TRIAL_REGISTRY",
                jurisdiction = "US",
                uri = URI.create("https://clinicaltrials.gov/api/v2/studies?query.term=$query&pageSize=10&format=json"),
            ),
            SourceEndpoint(
                name = "ANVISA",
                sourceType = "REGULATORY_MANUAL_REVIEW",
                jurisdiction = "BR",
                uri = URI.create("https://consultas.anvisa.gov.br/#/medicamentos/"),
                manualReviewOnly = true,
            ),
            SourceEndpoint(
                name = "EMA",
                sourceType = "REGULATORY_MANUAL_REVIEW",
                jurisdiction = "EU",
                uri = URI.create("https://www.ema.europa.eu/en/medicines"),
                manualReviewOnly = true,
            ),
            SourceEndpoint(
                name = "WADA",
                sourceType = "SPORT_REGULATORY_MANUAL_REVIEW",
                jurisdiction = "GLOBAL",
                uri = URI.create("https://www.wada-ama.org/en/prohibited-list"),
                manualReviewOnly = true,
            ),
        )
    }

    private fun encodeQuery(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")

    private fun encodePath(value: String): String = encodeQuery(value)

    private data class SourceEndpoint(
        val name: String,
        val sourceType: String,
        val jurisdiction: String?,
        val uri: URI,
        val manualReviewOnly: Boolean = false,
    ) {
        fun toFinding(contentHash: String, manual: Boolean): ResearchFinding = ResearchFinding(
            title = "$name research result requiring human review",
            sourceType = sourceType,
            url = uri.toASCIIString(),
            jurisdiction = jurisdiction,
            fetchedAt = Instant.now(),
            contentHash = contentHash,
            requiresManualReview = manual,
        )
    }

    private companion object {
        val ALLOWED_HOSTS = setOf(
            "rxnav.nlm.nih.gov",
            "pubchem.ncbi.nlm.nih.gov",
            "eutils.ncbi.nlm.nih.gov",
            "dailymed.nlm.nih.gov",
            "api.fda.gov",
            "clinicaltrials.gov",
            "consultas.anvisa.gov.br",
            "www.ema.europa.eu",
            "www.wada-ama.org",
        )
    }
}
