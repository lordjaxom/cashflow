package de.akvsoft.cashflow.frontend.usecase.rules

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@VaadinSessionScope
class RulesService(
    private val ruleRepository: RuleRepository,
    private val entryRepository: EntryRepository,
    private val balanceRepository: BalanceRepository
) {
    fun findAll(): List<Rule> = ruleRepository.findAllByOrderByNameAsc()

    fun create(): Rule = Rule(
        name = "",
        type = EntryType.REAL,
        amount = BigDecimal.ZERO,
        start = LocalDate.now(),
        end = null,
        schedule = Schedule(
            frequency = ScheduleFrequency.MONTH,
            interval = 1
        )
    )

    @Transactional
    fun save(rule: Rule): Rule {
        val previousRule = ruleRepository.findById(rule.id)
            .map { RuleScheduleSnapshot(it.start, it.schedule.frequency, it.schedule.interval) }
            .orElse(null)

        val savedRule = ruleRepository.save(rule)
        if (previousRule != null) {
            updateOverrideRuleDates(previousRule, savedRule)
        }
        return savedRule
    }

    @Transactional
    fun purgeExpiredRules(): Int {
        val purgeableRules = findPurgeableRules()
        ruleRepository.deleteAll(purgeableRules)
        return purgeableRules.size
    }

    fun countPurgeableRules(): Int = findPurgeableRules().size

    fun scheduleLabel(rule: Rule): String {
        val s = rule.schedule
        val freq = when (s.frequency) {
            ScheduleFrequency.MONTH -> "Monat"
            ScheduleFrequency.YEAR -> "Jahr"
        }
        val every = if (s.interval <= 1) "jeden" else "alle ${s.interval}"
        val freqs = if (s.interval <= 1) freq else "${freq}e"
        return "$every $freqs"
    }

    private fun findPurgeableRules(): List<Rule> {
        val currentBalanceStart = currentBalanceStart() ?: return emptyList()
        return ruleRepository.findAllByEndBefore(currentBalanceStart)
            .filterNot { entryRepository.existsByRuleId(it.id) }
    }

    private fun currentBalanceStart(): LocalDate? =
        balanceRepository.findFirstByOrderByMonthDesc()?.month?.withDayOfMonth(1)

    private fun updateOverrideRuleDates(previousRule: RuleScheduleSnapshot, rule: Rule) {
        entryRepository.findAllByRuleId(rule.id).forEach { entry ->
            val occurrenceIndex = previousRule.occurrenceIndex(entry.ruleDate ?: entry.date) ?: return@forEach
            entry.ruleDate = rule.occurrenceDate(occurrenceIndex)
            entry.name = rule.name
            entryRepository.save(entry)
        }
    }

    private fun Rule.occurrenceDate(index: Long): LocalDate =
        start.plus(
            schedule.interval.toLong() * index,
            when (schedule.frequency) {
                ScheduleFrequency.MONTH -> ChronoUnit.MONTHS
                ScheduleFrequency.YEAR -> ChronoUnit.YEARS
            }
        )

    private fun RuleScheduleSnapshot.occurrenceIndex(date: LocalDate): Long? {
        if (date < start) return null

        val between = when (frequency) {
            ScheduleFrequency.MONTH -> ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1))
            ScheduleFrequency.YEAR -> ChronoUnit.YEARS.between(start, date)
        }
        if (between < 0 || between % interval != 0L) return null

        val index = between / interval
        return index.takeIf { occurrenceDate(it) == date }
    }

    private fun RuleScheduleSnapshot.occurrenceDate(index: Long): LocalDate =
        start.plus(
            interval.toLong() * index,
            when (frequency) {
                ScheduleFrequency.MONTH -> ChronoUnit.MONTHS
                ScheduleFrequency.YEAR -> ChronoUnit.YEARS
            }
        )
}

private class RuleScheduleSnapshot(
    val start: LocalDate,
    val frequency: ScheduleFrequency,
    val interval: Int
)
