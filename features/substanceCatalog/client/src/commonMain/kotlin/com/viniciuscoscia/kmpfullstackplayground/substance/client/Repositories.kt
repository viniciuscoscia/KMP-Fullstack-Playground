package com.viniciuscoscia.kmpfullstackplayground.substance.client

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.CreateResearchJobRequest
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Dashboard
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.DraftRevision
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.PageResponse
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Problem
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Product
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJob
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewDecisionRequest
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SourceRef
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.SubstanceSummary
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.Tag
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

data class SubstanceAtlasConfig(
    val baseUrl: String,
    val adminToken: String? = null,
) {
    init {
        require(baseUrl.isBlank() || baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            "baseUrl must be blank for same-origin requests or use HTTP/HTTPS"
        }
    }

    internal val normalizedBaseUrl: String = baseUrl.trimEnd('/')
}

class SubstanceAtlasApiException(
    val statusCode: Int,
    val problem: Problem?,
    message: String,
) : IllegalStateException(message)

interface CatalogRepository {
    suspend fun dashboard(locale: String): Dashboard
    suspend fun products(query: String = "", page: Int = 0, size: Int = 25, locale: String = "pt-BR"): PageResponse<Product>
    suspend fun product(id: String, locale: String = "pt-BR"): Product
    suspend fun substances(
        query: String = "",
        tags: List<String> = emptyList(),
        page: Int = 0,
        size: Int = 25,
        locale: String = "pt-BR",
    ): PageResponse<SubstanceSummary>
    suspend fun substance(id: String, locale: String = "pt-BR"): Substance
    suspend fun compare(ids: List<String>, indicationId: String, locale: String = "pt-BR"): List<Substance>
    suspend fun tags(locale: String = "pt-BR"): List<Tag>
    suspend fun sources(page: Int = 0, size: Int = 25, locale: String = "pt-BR"): PageResponse<SourceRef>
}

interface ResearchRepository {
    suspend fun jobs(locale: String = "pt-BR"): List<ResearchJob>
    suspend fun createJob(terms: List<String>, locale: String, idempotencyKey: String): ResearchJob
    suspend fun job(id: String, locale: String = "pt-BR"): ResearchJob
    suspend fun draft(id: String, locale: String = "pt-BR"): DraftRevision
    suspend fun approve(id: String, reason: String, locale: String = "pt-BR"): DraftRevision
    suspend fun reject(id: String, reason: String, locale: String = "pt-BR"): DraftRevision
}

interface ReportRepository {
    suspend fun analyticsWorkbook(locale: String = "pt-BR"): ByteArray
}

class SubstanceAtlasRepositories(
    config: SubstanceAtlasConfig,
    client: HttpClient = defaultHttpClient(),
) {
    val catalog: CatalogRepository = HttpCatalogRepository(config, client)
    val research: ResearchRepository = HttpResearchRepository(config, client)
    val reports: ReportRepository = HttpReportRepository(config, client)
}

class HttpCatalogRepository(
    config: SubstanceAtlasConfig,
    client: HttpClient = defaultHttpClient(),
) : CatalogRepository {
    private val api = HttpApi(config, client)

    override suspend fun dashboard(locale: String): Dashboard =
        api.get("/api/v1/dashboard", locale)

    override suspend fun products(query: String, page: Int, size: Int, locale: String): PageResponse<Product> =
        api.get("/api/v1/products", locale) {
            parameter("query", query.takeIf { it.isNotBlank() })
            parameter("page", page)
            parameter("size", size)
        }

    override suspend fun product(id: String, locale: String): Product =
        api.get("/api/v1/products/$id", locale)

    override suspend fun substances(
        query: String,
        tags: List<String>,
        page: Int,
        size: Int,
        locale: String,
    ): PageResponse<SubstanceSummary> = api.get("/api/v1/substances", locale) {
        parameter("query", query.takeIf { it.isNotBlank() })
        parameter("tags", tags.takeIf { it.isNotEmpty() }?.joinToString(","))
        parameter("page", page)
        parameter("size", size)
    }

    override suspend fun substance(id: String, locale: String): Substance =
        api.get("/api/v1/substances/$id", locale)

    override suspend fun compare(ids: List<String>, indicationId: String, locale: String): List<Substance> {
        require(ids.size in 2..5) { "Compare requires between 2 and 5 substances" }
        return api.get("/api/v1/substances/compare", locale) {
            parameter("ids", ids.joinToString(","))
            parameter("indicationId", indicationId)
        }
    }

    override suspend fun tags(locale: String): List<Tag> = api.get("/api/v1/tags", locale)

    override suspend fun sources(page: Int, size: Int, locale: String): PageResponse<SourceRef> =
        api.get("/api/v1/sources", locale) {
            parameter("page", page)
            parameter("size", size)
        }
}

class HttpResearchRepository(
    config: SubstanceAtlasConfig,
    client: HttpClient = defaultHttpClient(),
) : ResearchRepository {
    private val api = HttpApi(config, client)

    override suspend fun jobs(locale: String): List<ResearchJob> =
        api.get("/api/v1/research-jobs", locale)

    override suspend fun createJob(terms: List<String>, locale: String, idempotencyKey: String): ResearchJob =
        api.post(
            path = "/api/v1/research-jobs",
            locale = locale,
            body = CreateResearchJobRequest(terms, locale),
            idempotencyKey = idempotencyKey,
            authenticated = true,
        )

    override suspend fun job(id: String, locale: String): ResearchJob =
        api.get("/api/v1/research-jobs/$id", locale)

    override suspend fun draft(id: String, locale: String): DraftRevision =
        api.get("/api/v1/drafts/$id", locale, authenticated = true)

    override suspend fun approve(id: String, reason: String, locale: String): DraftRevision =
        api.post("/api/v1/drafts/$id/approve", locale, ReviewDecisionRequest(reason), authenticated = true)

    override suspend fun reject(id: String, reason: String, locale: String): DraftRevision =
        api.post("/api/v1/drafts/$id/reject", locale, ReviewDecisionRequest(reason), authenticated = true)
}

class HttpReportRepository(
    config: SubstanceAtlasConfig,
    client: HttpClient = defaultHttpClient(),
) : ReportRepository {
    private val api = HttpApi(config, client)

    override suspend fun analyticsWorkbook(locale: String): ByteArray =
        api.bytes("/api/v1/reports/analytics.xlsx", locale)
}

class SubstanceAtlasFacade(
    baseUrl: String,
    adminToken: String? = null,
) {
    private val repositories = SubstanceAtlasRepositories(SubstanceAtlasConfig(baseUrl, adminToken))

    suspend fun dashboard(locale: String = "pt-BR"): Dashboard = repositories.catalog.dashboard(locale)

    suspend fun substances(query: String = "", locale: String = "pt-BR"): List<SubstanceSummary> =
        repositories.catalog.substances(query = query, locale = locale).items

    suspend fun substance(id: String, locale: String = "pt-BR"): Substance =
        repositories.catalog.substance(id, locale)

    suspend fun research(terms: List<String>, locale: String, idempotencyKey: String): ResearchJob =
        repositories.research.createJob(terms, locale, idempotencyKey)

    suspend fun researchJobs(locale: String = "pt-BR"): List<ResearchJob> = repositories.research.jobs(locale)

    suspend fun researchStatus(id: String, locale: String = "pt-BR"): ResearchJob =
        repositories.research.job(id, locale)
}

internal class HttpApi(
    private val config: SubstanceAtlasConfig,
    private val client: HttpClient,
) {
    suspend inline fun <reified T> get(
        path: String,
        locale: String,
        authenticated: Boolean = false,
        crossinline block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {},
    ): T {
        val response = client.get(config.normalizedBaseUrl + path) {
            commonHeaders(locale, authenticated)
            block()
        }
        return response.requireSuccess().body()
    }

    suspend inline fun <reified T, reified B> post(
        path: String,
        locale: String,
        body: B,
        idempotencyKey: String? = null,
        authenticated: Boolean = false,
    ): T {
        val response = client.post(config.normalizedBaseUrl + path) {
            commonHeaders(locale, authenticated)
            contentType(ContentType.Application.Json)
            idempotencyKey?.let { header("Idempotency-Key", it) }
            setBody(body)
        }
        return response.requireSuccess().body()
    }

    suspend fun bytes(path: String, locale: String): ByteArray {
        val response = client.get(config.normalizedBaseUrl + path) {
            commonHeaders(locale, authenticated = false)
            accept(ContentType.Application.OctetStream)
        }
        return response.requireSuccess().bodyAsBytes()
    }

    fun io.ktor.client.request.HttpRequestBuilder.commonHeaders(locale: String, authenticated: Boolean) {
        header(HttpHeaders.AcceptLanguage, locale)
        accept(ContentType.Application.Json)
        if (authenticated) {
            val token = requireNotNull(config.adminToken) { "An admin token is required for this operation" }
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

internal suspend fun HttpResponse.requireSuccess(): HttpResponse {
    if (status.isSuccess()) return this
    val text = bodyAsText()
    val problem = runCatching { defaultJson.decodeFromString<Problem>(text) }.getOrNull()
    throw SubstanceAtlasApiException(
        statusCode = status.value,
        problem = problem,
        message = problem?.detail ?: problem?.title ?: text.ifBlank { "Substance Atlas request failed" },
    )
}

private val defaultJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = true
}

private fun defaultHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(defaultJson)
    }
}
