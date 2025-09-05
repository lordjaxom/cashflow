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

    @Column(nullable = false)
    var amount: BigDecimal,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column
    var endDate: LocalDate?,

    @OneToOne(cascade = [CascadeType.ALL], optional = false, orphanRemoval = true)
    var schedule: Schedule,

    @Id
    val id: UUID = UUID.randomUUID()
)