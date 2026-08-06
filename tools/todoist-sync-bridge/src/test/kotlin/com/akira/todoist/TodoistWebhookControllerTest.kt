package com.akira.todoist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TodoistWebhookControllerTest {

    @Test
    fun `should process item completed webhook event with akira marker`() {
        val service = object : TodoistSyncService() {
            override fun syncRoadmap(roadmapSlug: String, dryRun: Boolean): RoadmapSyncSummary {
                return RoadmapSyncSummary(roadmapSlug, 0, 0, 0, 0)
            }
        }
        val controller = TodoistWebhookController(service)
        val payload = TodoistWebhookPayload(
            event_name = "item:completed",
            event_data = TodoistEventData(
                id = "12345",
                content = "Test Task",
                description = "akira:substance-atlas-roadmap#M6"
            )
        )

        val response = controller.handleWebhook(payload)
        assertEquals(200, response.statusCode.value())
        assertEquals("processed", response.body?.get("status"))
        assertEquals("substance-atlas-roadmap", response.body?.get("roadmapSlug"))
        assertEquals("M6", response.body?.get("milestoneId"))
    }
}
