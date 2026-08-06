package com.viniciuscoscia.kmpfullstackplayground.reporting

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogApplicationService
import com.viniciuscoscia.kmpfullstackplayground.research.ResearchApplicationService
import com.viniciuscoscia.kmpfullstackplayground.substance.contract.EvidenceLevel
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path

class ReportUnavailableException(message: String) : IllegalStateException(message)

@Service
class WorkbookExportService(
    private val catalog: CatalogApplicationService,
    private val research: ResearchApplicationService,
    @Value("\${substance-atlas.reporting.template-path}") templatePath: String,
) {
    private val template = Path.of(templatePath).normalize()

    fun export(locale: String): ByteArray {
        if (!Files.isRegularFile(template)) {
            throw ReportUnavailableException("The local analytics workbook template is not mounted")
        }
        val snapshot = catalog.exportSnapshot(locale)
        val jobs = research.recentJobs()
        Files.newInputStream(template).use { input ->
            WorkbookFactory.create(input).use { workbook ->
                val header = workbook.createHeaderStyle()
                writeDashboardData(workbook, snapshot.products.size, snapshot.substances.size, snapshot.sources.size, jobs.size, header)
                writeProducts(workbook.sheet("Products"), snapshot.products, header)
                writeSubstances(workbook.sheet("Substances"), snapshot.substances, header)
                writeAssessments(workbook.sheet("Assessments"), snapshot.substances, header)
                writeSources(workbook.sheet("Sources"), snapshot.sources, header)
                writeResearchQueue(workbook.sheet("Research Queue"), jobs, header)
                writeCodebook(workbook.sheet("Codebook"), header)
                return ByteArrayOutputStream().use { output ->
                    workbook.write(output)
                    output.toByteArray()
                }
            }
        }
    }

    private fun writeDashboardData(
        workbook: Workbook,
        products: Int,
        substances: Int,
        sources: Int,
        jobs: Int,
        header: CellStyle,
    ) {
        val sheet = workbook.sheet("Data & Targets")
        sheet.replaceRows(
            headers = listOf("Metric", "Value"),
            rows = listOf(
                listOf("Products", products),
                listOf("Substances", substances),
                listOf("Sources", sources),
                listOf("Research jobs", jobs),
            ),
            headerStyle = header,
        )
    }

    private fun writeProducts(sheet: Sheet, products: List<com.viniciuscoscia.kmpfullstackplayground.substance.contract.Product>, header: CellStyle) {
        sheet.replaceRows(
            headers = listOf("ID", "Product", "Brand", "Category", "Observed price", "Currency", "Captured at", "Parse status", "Substances"),
            rows = products.map { product ->
                listOf(
                    product.id,
                    product.name,
                    product.brand,
                    product.storeCategory,
                    product.observedPrice,
                    product.currency,
                    product.capturedAt,
                    product.parseStatus.name,
                    product.substances.joinToString("; ") { it.name },
                )
            },
            headerStyle = header,
        )
    }

    private fun writeSubstances(sheet: Sheet, substances: List<com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance>, header: CellStyle) {
        sheet.replaceRows(
            headers = listOf("ID", "Substance", "Description", "Tags", "Efficacy", "Risk", "Evidence", "Review"),
            rows = substances.map { substance ->
                val summary = substance.summary
                listOf(
                    summary.id,
                    summary.name,
                    summary.description,
                    summary.tags.joinToString("; ") { it.label },
                    summary.efficacyScore,
                    summary.riskScore,
                    summary.evidenceLevel.name,
                    summary.reviewStatus.name,
                )
            },
            headerStyle = header,
        )
    }

    private fun writeAssessments(sheet: Sheet, substances: List<com.viniciuscoscia.kmpfullstackplayground.substance.contract.Substance>, header: CellStyle) {
        val rows = substances.flatMap { substance ->
            substance.indications.map { assessment ->
                listOf(
                    substance.summary.name,
                    assessment.indication,
                    assessment.population,
                    assessment.outcome,
                    assessment.efficacyScore,
                    assessment.evidenceLevel.name,
                    assessment.rubricVersion,
                    assessment.reviewStatus.name,
                    assessment.rationale,
                )
            }
        }
        sheet.replaceRows(
            headers = listOf("Substance", "Indication", "Population", "Outcome", "Efficacy", "Evidence", "Rubric", "Review", "Rationale"),
            rows = rows,
            headerStyle = header,
        )
    }

    private fun writeSources(sheet: Sheet, sources: List<com.viniciuscoscia.kmpfullstackplayground.substance.contract.SourceRef>, header: CellStyle) {
        sheet.replaceRows(
            headers = listOf("ID", "Title", "Type", "Jurisdiction", "URL", "Published", "Fetched", "Hash"),
            rows = sources.map { source ->
                listOf(source.id, source.title, source.sourceType, source.jurisdiction, source.url, source.publishedAt, source.fetchedAt, source.contentHash)
            },
            headerStyle = header,
        )
    }

    private fun writeResearchQueue(sheet: Sheet, jobs: List<com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchJob>, header: CellStyle) {
        val rows = jobs.flatMap { job ->
            job.items.map { item ->
                listOf(job.id, job.status.name, item.requestedTerm, item.normalizedTerm, item.status.name, item.attempts, item.errorCode, job.updatedAt)
            }
        }
        sheet.replaceRows(
            headers = listOf("Job", "Job status", "Requested term", "Normalized term", "Item status", "Attempts", "Error", "Updated"),
            rows = rows,
            headerStyle = header,
        )
    }

    private fun writeCodebook(sheet: Sheet, header: CellStyle) {
        val rows = mutableListOf<List<Any?>>()
        rows += listOf("Efficacy", "null", "Insufficient direct evidence; never treated as zero")
        rows += listOf("Efficacy", "0", "Evidence of no meaningful benefit for the specified outcome")
        rows += listOf("Efficacy", "1-2", "Minimal or inconsistent")
        rows += listOf("Efficacy", "3-4", "Small")
        rows += listOf("Efficacy", "5-6", "Moderate")
        rows += listOf("Efficacy", "7-8", "Substantial")
        rows += listOf("Efficacy", "9-10", "Strong and repeatedly demonstrated")
        rows += listOf("Risk", "null", "Insufficient context or evidence")
        rows += listOf("Risk", "0-2", "Minimal to mild")
        rows += listOf("Risk", "3-4", "Relevant but generally manageable")
        rows += listOf("Risk", "5-6", "Significant")
        rows += listOf("Risk", "7-8", "Serious")
        rows += listOf("Risk", "9-10", "Extreme")
        EvidenceLevel.entries.forEach { rows += listOf("Evidence", it.name, "Independent from efficacy and risk scores") }
        sheet.replaceRows(listOf("Dimension", "Value", "Meaning"), rows, header)
    }
}

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val exporter: WorkbookExportService,
) {
    @GetMapping("/analytics.xlsx")
    fun analytics(
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ): ResponseEntity<ByteArray> = ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("substance-atlas-analytics.xlsx").build().toString(),
        )
        .body(exporter.export(locale))
}

private fun Workbook.sheet(name: String): Sheet = getSheet(name) ?: createSheet(name)

private fun Workbook.createHeaderStyle(): CellStyle = createCellStyle().apply {
    fillForegroundColor = IndexedColors.DARK_BLUE.index
    fillPattern = FillPatternType.SOLID_FOREGROUND
    setFont(createFont().apply {
        color = IndexedColors.WHITE.index
        bold = true
    })
}

private fun Sheet.replaceRows(headers: List<String>, rows: List<List<Any?>>, headerStyle: CellStyle) {
    while (physicalNumberOfRows > 0) {
        removeRow(getRow(lastRowNum))
    }
    createRow(0).also { row ->
        headers.forEachIndexed { index, value ->
            row.createCell(index).apply {
                setCellValue(value)
                cellStyle = headerStyle
            }
        }
    }
    rows.forEachIndexed { rowIndex, values ->
        createRow(rowIndex + 1).also { row ->
            values.forEachIndexed { columnIndex, value ->
                val cell = row.createCell(columnIndex)
                when (value) {
                    null -> cell.setBlank()
                    is Number -> cell.setCellValue(value.toDouble())
                    is Boolean -> cell.setCellValue(value)
                    else -> cell.setCellValue(value.toString())
                }
            }
        }
    }
    headers.indices.take(12).forEach { column -> autoSizeColumn(column) }
}
