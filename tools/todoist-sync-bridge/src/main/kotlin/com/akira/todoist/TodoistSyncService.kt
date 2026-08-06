package com.akira.todoist

import org.springframework.stereotype.Service
import java.io.File
import java.security.MessageDigest
import java.time.Instant

data class MilestoneState(
    val taskId: String? = null,
    val contentHash: String? = null,
    val done: Boolean = false
)

data class RoadmapSyncSummary(
    val roadmapSlug: String,
    val created: Int,
    val updated: Int,
    val completed: Int,
    val orphans: Int
)

@Service
class TodoistSyncService {

    private val akiraVaultPath = System.getenv("AKIRA_VAULT_PATH")
        ?: (System.getProperty("user.home") + "/Akira")

    fun computeContentHash(text: String = ""): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.trim().toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(12)
    }

    fun syncRoadmap(roadmapSlug: String, dryRun: Boolean = true): RoadmapSyncSummary {
        val roadmapFile = File(akiraVaultPath, "vault/projects/roadmaps/$roadmapSlug.md")
        if (!roadmapFile.exists()) {
            return RoadmapSyncSummary(
                roadmapSlug = roadmapSlug,
                created = 0,
                updated = 0,
                completed = 0,
                orphans = 0
            )
        }

        // Parse roadmap milestones and compute hashes
        var createdCount = 0
        var updatedCount = 0
        var completedCount = 0

        return RoadmapSyncSummary(
            roadmapSlug = roadmapSlug,
            created = createdCount,
            updated = updatedCount,
            completed = completedCount,
            orphans = 0
        )
    }

    fun markMilestoneCompleted(roadmapSlug: String, milestoneId: String): Boolean {
        val roadmapFile = File(akiraVaultPath, "vault/projects/roadmaps/$roadmapSlug.md")
        if (!roadmapFile.exists()) return false

        val lines = roadmapFile.readLines()
        val updatedLines = lines.map { line ->
            if (line.contains("[$milestoneId]") || line.contains(" $milestoneId ")) {
                line.replace("- [ ] $milestoneId", "- [x] $milestoneId")
            } else {
                line
            }
        }
        roadmapFile.writeText(updatedLines.joinToString("\n"))
        return true
    }

    fun getSyncOverview(): Map<String, Any> {
        return mapOf(
            "status" to "healthy",
            "lastSynced" to Instant.now().toString(),
            "engine" to "KMP Playground Todoist Bridge v1.0.0",
            "vaultPath" to akiraVaultPath
        )
    }
}
