package com.viniciuscoscia.kmpfullstackplayground

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogApplicationService
import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogExportSnapshot
import com.viniciuscoscia.kmpfullstackplayground.reporting.ReportUnavailableException
import com.viniciuscoscia.kmpfullstackplayground.reporting.WorkbookExportService
import com.viniciuscoscia.kmpfullstackplayground.research.ResearchApplicationService
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path

class WorkbookExportServiceTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `export keeps template sheets and adds atlas datasets`() {
        val template = tempDir.resolve("analytics-template.xlsx")
        XSSFWorkbook().use { workbook ->
            workbook.createSheet("Dashboard")
            workbook.createSheet("Data & Targets")
            workbook.createSheet("_Chart Helpers")
            Files.newOutputStream(template).use(workbook::write)
        }
        val catalog = mock(CatalogApplicationService::class.java)
        val research = mock(ResearchApplicationService::class.java)
        `when`(catalog.exportSnapshot("en")).thenReturn(CatalogExportSnapshot(emptyList(), emptyList(), emptyList()))
        `when`(research.recentJobs()).thenReturn(emptyList())

        val bytes = WorkbookExportService(catalog, research, template.toString()).export("en")

        WorkbookFactory.create(ByteArrayInputStream(bytes)).use { workbook ->
            assertThat(workbook.sheetIterator().asSequence().map { it.sheetName }.toList()).contains(
                "Dashboard",
                "Data & Targets",
                "_Chart Helpers",
                "Products",
                "Substances",
                "Assessments",
                "Sources",
                "Research Queue",
                "Codebook",
            )
            assertThat(workbook.getSheet("Data & Targets").getRow(1).getCell(0).stringCellValue)
                .isEqualTo("Products")
        }
    }

    @Test
    fun `missing proprietary template fails explicitly`() {
        val exporter = WorkbookExportService(
            mock(CatalogApplicationService::class.java),
            mock(ResearchApplicationService::class.java),
            tempDir.resolve("missing.xlsx").toString(),
        )

        assertThatThrownBy { exporter.export("pt-BR") }
            .isInstanceOf(ReportUnavailableException::class.java)
            .hasMessageContaining("not mounted")
    }
}
