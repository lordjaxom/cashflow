package de.akvsoft.cashflow.frontend.usecase.rules

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
@VaadinSessionScope
class RulesService(
    private val ruleRepository: RuleRepository
) {
    fun findAll(): List<Rule> = ruleRepository.findAll()

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

    fun scheduleLabel(rule: Rule): String {
        val s = rule.schedule
        val freq = when (s.frequency) {
            ScheduleFrequency.MONTH -> "Monat"
            ScheduleFrequency.YEAR -> "Jahr"
        }
        val every = if (s.interval <= 1) "jeden" else "alle ${s.interval}"
        return "$every $freq(e)"
    }
}
