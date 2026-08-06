package com.viniciuscoscia.kmpfullstackplayground

import com.viniciuscoscia.kmpfullstackplayground.catalog.importer.MarketplaceImportService
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportObservationJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Path

@SpringBootTest
@ActiveProfiles("test")
class MarketplaceImportIntegrationTest(
    @Autowired private val service: MarketplaceImportService,
    @Autowired private val observations: CatalogImportObservationJpaRepository,
) {
    @Test
    fun `private capture import is atomic complete and idempotent`() {
        val sourcePath = System.getenv("SUBSTANCE_ATLAS_CAPTURE_AUDIT_PATH")
        assumeTrue(!sourcePath.isNullOrBlank(), "Private capture is intentionally unavailable in CI")

        val first = service.importCapture(Path.of(sourcePath), "Local capture audit", 447)
        val replay = service.importCapture(Path.of(sourcePath), "Local capture audit", 447)

        assertThat(first.status).isEqualTo(CatalogImportStatus.COMPLETED)
        assertThat(first.observationCount).isEqualTo(447)
        assertThat(first.productCount).isEqualTo(435)
        assertThat(replay.importId).isEqualTo(first.importId)
        assertThat(observations.count()).isEqualTo(447)
    }
}
