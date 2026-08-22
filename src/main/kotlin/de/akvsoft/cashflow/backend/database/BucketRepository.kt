package de.akvsoft.cashflow.backend.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BucketRepository : JpaRepository<Bucket, UUID> {
    fun findAllByOrderByNameAsc(): List<Bucket>
    fun existsByNameIgnoreCase(name: String): Boolean
}
