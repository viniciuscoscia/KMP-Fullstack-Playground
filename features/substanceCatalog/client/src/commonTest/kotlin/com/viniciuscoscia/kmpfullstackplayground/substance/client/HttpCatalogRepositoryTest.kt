package com.viniciuscoscia.kmpfullstackplayground.substance.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HttpCatalogRepositoryTest {
    @Test
    fun substancesPreserveUnknownScoresAndLocale() = runTest {
        var language: String? = null
        val engine = MockEngine { request ->
            language = request.headers[HttpHeaders.AcceptLanguage]
            respond(
                content = """{"items":[{"id":"s1","name":"Example","description":"Synthetic","tags":[],"efficacyScore":null,"riskScore":null,"evidenceLevel":"INSUFFICIENT","reviewStatus":"APPROVED"}],"page":0,"size":25,"total":1}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val page = HttpCatalogRepository(SubstanceAtlasConfig("http://localhost:8080"), client)
            .substances(locale = "pt-BR")

        assertEquals("pt-BR", language)
        assertEquals(1, page.total)
        assertNull(page.items.single().efficacyScore)
        assertNull(page.items.single().riskScore)
    }
}
