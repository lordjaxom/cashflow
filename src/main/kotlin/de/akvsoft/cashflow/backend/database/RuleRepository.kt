package de.akvsoft.cashflow.backend.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface RuleRepository : JpaRepository<Rule, UUID> {

    fun findAllByOrderByNameAsc(): List<Rule>

    fun findAllByEndBefore(end: LocalDate): List<Rule>
}
