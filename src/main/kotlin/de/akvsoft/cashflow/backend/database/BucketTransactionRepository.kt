package de.akvsoft.cashflow.backend.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BucketTransactionRepository : JpaRepository<BucketTransaction, UUID> {
    fun findAllByBucketOrderByDateAsc(bucket: Bucket): List<BucketTransaction>

    fun deleteAllByBucket(bucket: Bucket)
}
