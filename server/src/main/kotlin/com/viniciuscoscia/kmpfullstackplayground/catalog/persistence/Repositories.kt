package com.viniciuscoscia.kmpfullstackplayground.catalog.persistence

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ReviewStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface SubstanceJpaRepository : JpaRepository<SubstanceEntity, UUID> {
    fun findByCanonicalNameIgnoreCase(canonicalName: String): SubstanceEntity?

    fun findByCanonicalNameContainingIgnoreCase(canonicalName: String, pageable: Pageable): Page<SubstanceEntity>

    @Query(
        """
        select distinct s from SubstanceEntity s
        join s.tags t
        where lower(s.canonicalName) like lower(concat('%', :query, '%'))
          and t.slug in :tags
        """,
    )
    fun searchByNameAndTags(
        @Param("query") query: String,
        @Param("tags") tags: Collection<String>,
        pageable: Pageable,
    ): Page<SubstanceEntity>

    fun countByRiskOverallScoreGreaterThanEqual(score: Int): Long

    fun countByReviewStatusIn(statuses: Collection<ReviewStatus>): Long

    @Query("select s.evidenceLevel, count(s) from SubstanceEntity s group by s.evidenceLevel")
    fun evidenceDistribution(): List<Array<Any>>

    @Query("select count(distinct s) from SubstanceEntity s join s.evidenceClaims c")
    fun countWithEvidence(): Long
}

interface ProductJpaRepository : JpaRepository<ProductEntity, UUID> {
    fun findByOriginalNameContainingIgnoreCase(originalName: String, pageable: Pageable): Page<ProductEntity>
}

interface BrandJpaRepository : JpaRepository<BrandEntity, UUID> {
    fun findByNameIgnoreCase(name: String): BrandEntity?
}

interface CatalogImportJpaRepository : JpaRepository<CatalogImportEntity, UUID> {
    fun findBySourceHash(sourceHash: String): CatalogImportEntity?
}

interface CatalogImportObservationJpaRepository : JpaRepository<CatalogImportObservationEntity, UUID>

interface TagJpaRepository : JpaRepository<TagEntity, UUID> {
    fun findAllByOrderByGroupAscLabelPtAsc(): List<TagEntity>
}

interface SourceJpaRepository : JpaRepository<SourceEntity, UUID>
