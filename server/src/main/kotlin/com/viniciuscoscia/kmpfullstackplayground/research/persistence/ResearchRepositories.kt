package com.viniciuscoscia.kmpfullstackplayground.research.persistence

import com.viniciuscoscia.kmpfullstackplayground.substance.contract.ResearchItemStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ResearchJobJpaRepository : JpaRepository<ResearchJobEntity, UUID> {
    @EntityGraph(attributePaths = ["items"])
    fun findByIdempotencyKey(idempotencyKey: String): ResearchJobEntity?

    @EntityGraph(attributePaths = ["items"])
    @Query("select j from ResearchJobEntity j where j.id = :id")
    fun findDetailedById(@Param("id") id: UUID): ResearchJobEntity?

    @EntityGraph(attributePaths = ["items"])
    fun findTop100ByOrderByCreatedAtDesc(): List<ResearchJobEntity>
}

interface ResearchJobItemJpaRepository : JpaRepository<ResearchJobItemEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select i from ResearchJobItemEntity i
        where i.status = :status
          and (i.nextRetryAt is null or i.nextRetryAt <= :now)
        order by i.createdAt asc
        """,
    )
    fun lockNext(
        @Param("status") status: ResearchItemStatus,
        @Param("now") now: Instant,
        pageable: Pageable,
    ): List<ResearchJobItemEntity>
}

interface DraftRevisionJpaRepository : JpaRepository<DraftRevisionEntity, UUID> {
    @Query("select coalesce(max(d.revision), 0) from DraftRevisionEntity d where d.substanceId = :substanceId")
    fun maxRevision(@Param("substanceId") substanceId: UUID): Int
}

interface ReviewDecisionJpaRepository : JpaRepository<ReviewDecisionEntity, UUID>
