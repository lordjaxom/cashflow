package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

enum class EntryType {
    REAL, ESTIMATE, FLATRATE
}

@Entity
class Entry(
    @Column(nullable = false)
    var date: LocalDate,

    @Column(nullable = false)
    var amount: BigDecimal,

    @Column(nullable = false)
    var type: EntryType,

    @OneToOne(optional=true)
    var rule: Rule? = null,

    @Id
    val id: UUID = UUID.randomUUID()
)