package com.viniciuscoscia.kmpfullstackplayground.catalog.web

import com.viniciuscoscia.kmpfullstackplayground.catalog.CatalogApplicationService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/v1")
class CatalogController(
    private val catalog: CatalogApplicationService,
) {
    @GetMapping("/dashboard")
    fun dashboard() = catalog.dashboard()

    @GetMapping("/products")
    fun products(
        @RequestParam(defaultValue = "") @Size(max = 120) query: String,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "25") @Min(1) @Max(100) size: Int,
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.listProducts(query, page, size, locale)

    @GetMapping("/products/{id}")
    fun product(
        @PathVariable id: UUID,
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.product(id, locale)

    @GetMapping("/substances")
    fun substances(
        @RequestParam(defaultValue = "") @Size(max = 120) query: String,
        @RequestParam(defaultValue = "") tags: List<String>,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "25") @Min(1) @Max(100) size: Int,
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.listSubstances(query, tags.filter(String::isNotBlank), page, size, locale)

    @GetMapping("/substances/compare")
    fun compare(
        @RequestParam ids: List<UUID>,
        @RequestParam indicationId: UUID,
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.compare(ids, indicationId, locale)

    @GetMapping("/substances/{id}")
    fun substance(
        @PathVariable id: UUID,
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.substance(id, locale)

    @GetMapping("/tags")
    fun tags(
        @RequestHeader(name = "Accept-Language", defaultValue = "pt-BR") locale: String,
    ) = catalog.listTags(locale)

    @GetMapping("/sources")
    fun sources(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "25") @Min(1) @Max(100) size: Int,
    ) = catalog.listSources(page, size)

    @GetMapping("/")
    fun root(): ResponseEntity<Void> = ResponseEntity.status(308).location(URI.create("/actuator/health")).build()
}
