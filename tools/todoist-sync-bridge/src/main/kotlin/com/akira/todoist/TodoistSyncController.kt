package com.akira.todoist

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/todoist")
class TodoistSyncController(
    private val syncService: TodoistSyncService
) {

    @GetMapping("/overview")
    fun getOverview(): Map<String, Any> {
        return syncService.getSyncOverview()
    }

    @PostMapping("/sync")
    fun triggerSync(@RequestParam(defaultValue = "true") dryRun: Boolean): Map<String, Any> {
        val roadmaps = listOf("substance-atlas-roadmap", "income-sprint-roadmap", "algolens-roadmap", "kmp-fullstack-playground-roadmap", "local-priority-os-roadmap", "mba-roadmap")
        val summaries = roadmaps.map { syncService.syncRoadmap(it, dryRun) }
        return mapOf(
            "status" to "success",
            "dryRun" to dryRun,
            "roadmaps" to summaries
        )
    }
}
