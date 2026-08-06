package com.viniciuscoscia.kmpfullstackplayground

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun `catalog reads are public and localized`() {
        mockMvc.get("/api/v1/substances") {
            header(HttpHeaders.ACCEPT_LANGUAGE, "en")
        }.andExpect {
            status { isOk() }
            jsonPath("$.total") { value(6) }
            jsonPath("$.items[0].description") { isNotEmpty() }
        }
    }

    @Test
    fun `research mutations require local admin token`() {
        mockMvc.post("/api/v1/research-jobs") {
            header("Idempotency-Key", "api-contract-unauthorized")
            contentType = MediaType.APPLICATION_JSON
            content = """{"terms":["Synthetic term"],"locale":"en"}"""
        }.andExpect {
            status { isUnauthorized() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
        }

        mockMvc.post("/api/v1/research-jobs") {
            header(HttpHeaders.AUTHORIZATION, "Bearer test-admin-token")
            header("Idempotency-Key", "api-contract-authorized")
            contentType = MediaType.APPLICATION_JSON
            content = """{"terms":["Synthetic term"],"locale":"en"}"""
        }.andExpect {
            status { isAccepted() }
            jsonPath("$.status") { value("QUEUED") }
            jsonPath("$.items[0].requestedTerm") { value("Synthetic term") }
        }
    }

    @Test
    fun `draft reads are protected before resource lookup`() {
        mockMvc.get("/api/v1/drafts/${UUID.randomUUID()}").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `validation and unavailable reports use problem details`() {
        mockMvc.get("/api/v1/substances") {
            param("size", "101")
        }.andExpect {
            status { isBadRequest() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(400) }
        }

        mockMvc.get("/api/v1/reports/analytics.xlsx").andExpect {
            status { isServiceUnavailable() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(503) }
        }
    }
}
