package de.akvsoft.cashflow.backend.database

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
class Rule(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var type: EntryType,

    @Column(nullable = false, columnDefinition = "DECIMAL(18,2)")
    var amount: BigDecimal,

    @Column(name = "start_", nullable = false)
    var start: LocalDate,

    @Column(name = "end_")
    var end: LocalDate?,

    @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true)
    var schedule: Schedule,

    @Id
    val id: UUID = UUID.randomUUID()
)