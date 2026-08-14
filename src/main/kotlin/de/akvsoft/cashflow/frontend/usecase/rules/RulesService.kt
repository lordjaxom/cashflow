package de.akvsoft.cashflow.frontend.usecase.rules

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

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

    fun save(rule: Rule): Rule {
        return ruleRepository.save(rule)
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
}
