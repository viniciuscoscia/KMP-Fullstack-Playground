package com.viniciuscoscia.kmpfullstackplayground

import com.viniciuscoscia.kmpfullstackplayground.assistant.SubstanceAtlasMcpTools
import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogApplicationService
import com.viniciuscoscia.kmpfullstackplayground.research.ResearchApplicationService
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchStatus
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTest(
    @Autowired private val catalog: CatalogApplicationService,
    @Autowired private val research: ResearchApplicationService,
    @Autowired private val mcpTools: SubstanceAtlasMcpTools,
) {
    @Test
    fun `application modules follow declared boundaries`() {
        ApplicationModules.of(
            SubstanceAtlasApplication::class.java,
            resideInAnyPackage("..substance.contract.."),
        ).verify()
    }

    @Test
    fun `synthetic fixture is readable through catalog and MCP adapters`() {
        val page = catalog.listSubstances("Atlas", emptyList(), page = 0, size = 20, locale = "en")

        assertThat(page.total).isEqualTo(1)
        assertThat(page.items).allMatch { it.reviewStatus.name == "APPROVED" }
        assertThat(mcpTools.searchSubstances("Atlas", 0, 20, "en").items).hasSize(1)
    }

    @Test
    fun `research creation is idempotent and does not publish results`() {
        val first = research.createJob(listOf("Synthetic candidate"), "en", "application-test-key")
        val replay = research.createJob(listOf("Different retry body"), "en", "application-test-key")

        assertThat(replay.id).isEqualTo(first.id)
        assertThat(replay.status).isEqualTo(ResearchStatus.QUEUED)
        assertThat(replay.items).hasSize(1)
        assertThat(replay.items.single().requestedTerm).isEqualTo("Synthetic candidate")
    }
}
