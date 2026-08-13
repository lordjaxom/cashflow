package de.akvsoft.cashflow.backend.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface BalanceRepository: JpaRepository<Balance, UUID> {

    fun findFirstByOrderByMonthDesc(): Balance?

    fun findFirstByMonth(month: LocalDate): Balance?
}
