package com.viniciuscoscia.kmpfullstackplayground

import com.viniciuscoscia.kmpfullstackplayground.catalog.importer.MarketplaceCaptureParser
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.PriceObservationRole
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ParseStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class MarketplaceCaptureParserTest {
    private val parser = MarketplaceCaptureParser()

    @Test
    fun `accounts for sale prices and preserves supplemental card text`() {
        val capture = parser.parse(
            """
            Search for items
            All
            Example One (10mg) Fixture Labs
            Example One (10mg) Fixture Labs
            Synthetic
            R$100.00
            20% OFF
            Example Two (20mg) Fixture Labs
            Example Two (20mg) Fixture Labs
            Synthetic
            R$80.00
            R$100.00
            Component A 10mg Component B 10mg
            Example Three (30mg) Fixture Labs
            Example Three (30mg) Fixture Labs
            Synthetic
            R$120.00
            Security tip: synthetic footer
            Developed by
            Back to top
            """.trimIndent(),
        )

        assertThat(capture.products).hasSize(3)
        assertThat(capture.observationCount).isEqualTo(4)
        assertThat(capture.products).allMatch { it.parseStatus == ParseStatus.PARSED }
        assertThat(capture.products[1].displayMarker).isEqualTo("20% OFF")
        assertThat(capture.products[1].prices.map { it.role }).containsExactly(
            PriceObservationRole.CURRENT_PRICE,
            PriceObservationRole.COMPARE_AT_PRICE,
        )
        assertThat(capture.products[1].supplementalText).isEqualTo("Component A 10mg Component B 10mg")
        assertThat(capture.products[2].supplementalText).isNull()
    }

    @Test
    fun `local private capture has complete explicit accounting`() {
        val path = System.getenv("SUBSTANCE_ATLAS_CAPTURE_AUDIT_PATH")
        assumeTrue(!path.isNullOrBlank(), "Private capture is intentionally unavailable in CI")

        val capture = parser.parse(Files.readString(Path.of(path)))

        assertThat(capture.observationCount).isEqualTo(447)
        assertThat(capture.products).hasSize(435)
        assertThat(capture.products).allMatch { it.parseStatus == ParseStatus.PARSED }
    }
}
