package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
class BucketTransaction(
    @ManyToOne(optional = false)
    var bucket: Bucket,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(nullable = false, columnDefinition = "DECIMAL(18,2)")
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: BucketTransactionType,

    @Column(nullable = false)
    var projectionEntryId: UUID,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Id
    val id: UUID = UUID.randomUUID()
)

enum class BucketTransactionType {
    ADD, REMOVE
}

fun BucketTransactionType.toDisplayString() = when (this) {
    BucketTransactionType.ADD -> "geparkt"
    BucketTransactionType.REMOVE -> "entparkt"
}
