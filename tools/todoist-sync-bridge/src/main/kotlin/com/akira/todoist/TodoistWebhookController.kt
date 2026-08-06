package com.akira.todoist

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class TodoistEventData(
    val id: String? = null,
    val content: String? = null,
    val description: String? = null
)

data class TodoistWebhookPayload(
    val event_name: String? = null,
    val event_data: TodoistEventData? = null
)

@RestController
@RequestMapping("/api/v1/todoist")
class TodoistWebhookController(
    private val syncService: TodoistSyncService
) {

    @PostMapping("/webhook")
    fun handleWebhook(@RequestBody payload: TodoistWebhookPayload): ResponseEntity<Map<String, Any>> {
        val eventName = payload.event_name ?: ""
        if (eventName == "item:completed" || eventName == "item:updated") {
            val description = payload.event_data?.description ?: ""
            val regex = Regex("""akira:([a-zA-Z0-9_-]+)#(M\d+)""")
            val match = regex.find(description)
            if (match != null) {
                val roadmapSlug = match.groupValues[1]
                val milestoneId = match.groupValues[2]

                if (eventName == "item:completed") {
                    val updated = syncService.markMilestoneCompleted(roadmapSlug, milestoneId)
                    return ResponseEntity.ok(
                        mapOf(
                            "status" to "processed",
                            "event" to eventName,
                            "roadmapSlug" to roadmapSlug,
                            "milestoneId" to milestoneId,
                            "vaultUpdated" to updated
                        )
                    )
                }
            }
        }

        return ResponseEntity.ok(
            mapOf(
                "status" to "ignored",
                "reason" to "event not actionable or marker missing"
            )
        )
    }
}
