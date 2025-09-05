package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID

@Entity
class Balance(
    @Column(nullable = false)
    var month: YearMonth,

    @Column(nullable = false, columnDefinition = "DECIMAL(18,2)")
    var balance: BigDecimal,

    @Id
    val id: UUID = UUID.randomUUID()
)
