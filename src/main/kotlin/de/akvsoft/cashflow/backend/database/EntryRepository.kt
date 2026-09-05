package de.akvsoft.cashflow.backend.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface EntryRepository : JpaRepository<Entry, UUID>{

    fun findAllByOrderByDateAsc(): List<Entry>

    fun findAllByRuleId(ruleId: UUID): List<Entry>

    fun existsByRuleId(ruleId: UUID): Boolean

    @Query("delete from Entry a where a.date < :date")
    @Modifying
    fun deleteByDateBefore(date: LocalDate): Int
}
