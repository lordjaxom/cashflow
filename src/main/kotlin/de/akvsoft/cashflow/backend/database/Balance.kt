package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
class Balance(
    @Column(name = "month_", nullable = false)
    var month: LocalDate,

    @Column(nullable = false, columnDefinition = "DECIMAL(18,2)")
    var balance: BigDecimal,

    @Id
    val id: UUID = UUID.randomUUID()
)
