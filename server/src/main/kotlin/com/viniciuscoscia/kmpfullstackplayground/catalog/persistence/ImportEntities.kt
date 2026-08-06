package com.viniciuscoscia.kmpfullstackplayground.catalog.persistence

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ParseStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class CatalogImportStatus { PROCESSING, COMPLETED, NEEDS_INPUT, FAILED }

enum class PriceObservationRole { CURRENT_PRICE, COMPARE_AT_PRICE }

@Entity
@Table(name = "catalog_import")
class CatalogImportEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "source_name", nullable = false, length = 160)
    var sourceName: String = "",
    @Column(name = "source_hash", nullable = false, unique = true, length = 128)
    var sourceHash: String = "",
    @Column(name = "expected_observation_count", nullable = false)
    var expectedObservationCount: Int = 0,
    @Column(name = "observed_observation_count", nullable = false)
    var observedObservationCount: Int = 0,
    @Column(name = "product_count", nullable = false)
    var productCount: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: CatalogImportStatus = CatalogImportStatus.PROCESSING,
    @Column(name = "captured_at", nullable = false)
    var capturedAt: Instant = Instant.now(),
    @Column(name = "started_at", nullable = false)
    var startedAt: Instant = Instant.now(),
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
)

@Entity
@Table(name = "catalog_import_observation")
class CatalogImportObservationEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_import_id", nullable = false)
    var catalogImport: CatalogImportEntity = CatalogImportEntity(),
    @Column(name = "source_ordinal", nullable = false)
    var sourceOrdinal: Int = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: ProductEntity? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "observation_role", nullable = false, length = 32)
    var observationRole: PriceObservationRole = PriceObservationRole.CURRENT_PRICE,
    @Column(name = "observed_price", nullable = false, precision = 12, scale = 2)
    var observedPrice: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, length = 3)
    var currency: String = "BRL",
    @Enumerated(EnumType.STRING)
    @Column(name = "parse_status", nullable = false, length = 32)
    var parseStatus: ParseStatus = ParseStatus.NEEDS_INPUT,
    @Column(name = "display_marker", length = 120)
    var displayMarker: String? = null,
    @Column(name = "supplemental_text")
    var supplementalText: String? = null,
    @Column(name = "error_code", length = 80)
    var errorCode: String? = null,
)
