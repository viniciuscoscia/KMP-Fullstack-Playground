package com.viniciuscoscia.kmpfullstackplayground.catalog.importer

import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.BrandEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.BrandJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportObservationEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportObservationJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.CatalogImportStatus
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.PriceObservationRole
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.ProductEntity
import com.viniciuscoscia.kmpfullstackplayground.catalog.persistence.ProductJpaRepository
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ParseStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

data class MarketplaceCapture(
    val products: List<MarketplaceProductCard>,
) {
    val observationCount: Int = products.sumOf { it.prices.size }
}

data class MarketplaceProductCard(
    val ordinal: Int,
    val name: String,
    val category: String?,
    val inferredBrand: String,
    val prices: List<CapturePrice>,
    val displayMarker: String?,
    val supplementalText: String?,
    val parseStatus: ParseStatus,
)

data class CapturePrice(
    val role: PriceObservationRole,
    val amount: BigDecimal,
)

data class CatalogImportSummary(
    val importId: UUID,
    val sourceHash: String,
    val observationCount: Int,
    val productCount: Int,
    val status: CatalogImportStatus,
)

@Component
class MarketplaceCaptureParser {
    fun parse(content: String): MarketplaceCapture {
        val lines = content.lineSequence().map(String::trim).toList()
        val pairStarts = (0 until lines.lastIndex).filter { index ->
            val first = normalize(lines[index])
            first.isNotEmpty() && first == normalize(lines[index + 1])
        }
        require(pairStarts.isNotEmpty()) { "No duplicated product-card names were found" }

        val products = pairStarts.mapIndexed { productIndex, start ->
            val nextStart = pairStarts.getOrNull(productIndex + 1) ?: lines.size
            val name = normalize(lines[start])
            val category = lines.getOrNull(start + 2)?.takeIf(String::isNotBlank)
            val bodyStart = (start + 3).coerceAtMost(nextStart)
            val body = lines.subList(bodyStart, nextStart)
            val priceLines = body.takeWhile(::isPrice)
            val prices = priceLines.mapIndexed { priceIndex, raw ->
                CapturePrice(
                    role = if (priceIndex == 0) PriceObservationRole.CURRENT_PRICE else PriceObservationRole.COMPARE_AT_PRICE,
                    amount = parsePrice(raw),
                )
            }
            val supplemental = body.drop(priceLines.size)
                .filterNot(::isDisplayMarker)
                .filterNot(::isFooter)
                .filter(String::isNotBlank)
                .joinToString("\n")
                .takeIf(String::isNotBlank)
            val marker = lines.getOrNull(start - 1)?.takeIf(::isDisplayMarker)
            val status = if (name.isBlank() || category == null || prices.size !in 1..2) {
                ParseStatus.NEEDS_INPUT
            } else {
                ParseStatus.PARSED
            }
            MarketplaceProductCard(
                ordinal = productIndex + 1,
                name = name,
                category = category,
                inferredBrand = inferBrand(name, category),
                prices = prices,
                displayMarker = marker,
                supplementalText = supplemental,
                parseStatus = status,
            )
        }
        return MarketplaceCapture(products)
    }

    private fun normalize(value: String): String = value.trim().replace(Regex("\\s+"), " ")

    private fun inferBrand(name: String, category: String?): String {
        val suffix = name.substringAfterLast(") ", missingDelimiterValue = "").trim()
        return (suffix.takeIf(String::isNotBlank) ?: category ?: "Unknown").take(160)
    }

    private fun isPrice(value: String): Boolean = PRICE.matches(value)

    private fun parsePrice(value: String): BigDecimal = value.removePrefix("R$").replace(",", "").toBigDecimal()

    private fun isDisplayMarker(value: String): Boolean = DISCOUNT.matches(value) ||
        value.contains("IMAGEM ILUSTRATIVA", ignoreCase = true) ||
        value.contains("IMAGEM ILUTRATIVA", ignoreCase = true)

    private fun isFooter(value: String): Boolean = value.startsWith("Security tip:") ||
        value == "Developed by" || value == "Back to top" || value == "Medical Brasil"

    private companion object {
        val PRICE = Regex("^R\\$[0-9,]+\\.[0-9]{2}$")
        val DISCOUNT = Regex("^[0-9]{1,3}% OFF$", RegexOption.IGNORE_CASE)
    }
}

@Service
class MarketplaceImportService(
    private val parser: MarketplaceCaptureParser,
    private val imports: CatalogImportJpaRepository,
    private val observations: CatalogImportObservationJpaRepository,
    private val products: ProductJpaRepository,
    private val brands: BrandJpaRepository,
) {
    @Transactional
    fun importCapture(path: Path, sourceName: String, expectedObservations: Int): CatalogImportSummary {
        require(Files.isRegularFile(path)) { "Configured catalog import file is not mounted" }
        val bytes = Files.readAllBytes(path)
        val sourceHash = sha256(bytes)
        imports.findBySourceHash(sourceHash)?.let { return it.toSummary() }

        val capture = parser.parse(bytes.toString(StandardCharsets.UTF_8))
        require(capture.observationCount == expectedObservations) {
            "Expected $expectedObservations price observations but parsed ${capture.observationCount}"
        }
        val capturedAt = Files.getLastModifiedTime(path).toInstant()
        val import = imports.save(
            CatalogImportEntity(
                id = stableId("import:$sourceHash"),
                sourceName = sourceName.take(160),
                sourceHash = sourceHash,
                expectedObservationCount = expectedObservations,
                observedObservationCount = capture.observationCount,
                productCount = capture.products.size,
                status = CatalogImportStatus.PROCESSING,
                capturedAt = capturedAt,
            ),
        )
        val brandCache = mutableMapOf<String, BrandEntity>()
        var sourceOrdinal = 0
        capture.products.forEach { card ->
            val brand = brandCache.getOrPut(card.inferredBrand.lowercase()) {
                brands.findByNameIgnoreCase(card.inferredBrand) ?: brands.save(
                    BrandEntity(id = stableId("brand:${card.inferredBrand.lowercase()}"), name = card.inferredBrand),
                )
            }
            val product = products.save(
                ProductEntity(
                    id = stableId("$sourceHash:product:${card.ordinal}"),
                    brand = brand,
                    originalName = card.name.take(500),
                    storeCategory = card.category?.take(160),
                    observedPrice = card.prices.firstOrNull()?.amount,
                    currency = "BRL",
                    listingUrl = null,
                    capturedAt = capturedAt,
                    parseStatus = card.parseStatus,
                ),
            )
            card.prices.forEachIndexed { priceIndex, price ->
                sourceOrdinal += 1
                observations.save(
                    CatalogImportObservationEntity(
                        id = stableId("$sourceHash:observation:$sourceOrdinal"),
                        catalogImport = import,
                        sourceOrdinal = sourceOrdinal,
                        product = product,
                        observationRole = price.role,
                        observedPrice = price.amount,
                        parseStatus = card.parseStatus,
                        displayMarker = card.displayMarker.takeIf { priceIndex == 0 },
                        supplementalText = card.supplementalText.takeIf { priceIndex == 0 },
                        errorCode = if (card.parseStatus == ParseStatus.PARSED) null else "CARD_NEEDS_INPUT",
                    ),
                )
            }
        }
        import.status = if (capture.products.all { it.parseStatus == ParseStatus.PARSED }) {
            CatalogImportStatus.COMPLETED
        } else {
            CatalogImportStatus.NEEDS_INPUT
        }
        import.completedAt = Instant.now()
        return imports.save(import).toSummary()
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private fun stableId(value: String): UUID = UUID.nameUUIDFromBytes(value.toByteArray(StandardCharsets.UTF_8))

    private fun CatalogImportEntity.toSummary(): CatalogImportSummary = CatalogImportSummary(
        importId = id,
        sourceHash = sourceHash,
        observationCount = observedObservationCount,
        productCount = productCount,
        status = status,
    )
}

@Component
@ConditionalOnProperty(prefix = "substance-atlas.import", name = ["enabled"], havingValue = "true")
class MarketplaceImportRunner(
    private val service: MarketplaceImportService,
    @Value("\${substance-atlas.import.source-path}") private val sourcePath: String,
    @Value("\${substance-atlas.import.source-name}") private val sourceName: String,
    @Value("\${substance-atlas.import.expected-observations}") private val expectedObservations: Int,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        val summary = service.importCapture(Path.of(sourcePath).normalize(), sourceName, expectedObservations)
        logger.info(
            "Catalog import ready: observations={}, products={}, status={}",
            summary.observationCount,
            summary.productCount,
            summary.status,
        )
    }

    private companion object {
        val logger = LoggerFactory.getLogger(MarketplaceImportRunner::class.java)
    }
}
