package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryRepository
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.Rule
import de.akvsoft.cashflow.backend.database.RuleRepository
import de.akvsoft.cashflow.backend.database.ScheduleFrequency
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
@VaadinSessionScope
class CalculateService(
    private val entryRepository: EntryRepository,
    private val ruleRepository: RuleRepository
) {

    fun calculate(deadline: LocalDate): List<Calculation> {
        val entries = entryRepository.findAllByOrderByDateAsc()
        val firstEntry = entries.firstOrNull() ?: return emptyList()
        if (firstEntry.rule != null) throw IllegalStateException("Der erste Eintrag darf kein Regel-Eintrag sein.")

        return buildList {
            val rules = ruleRepository.findAll()
            val startDate = entries.first().date
            var currentDate = startDate
            var currentBalance = BigDecimal.ZERO
            while (currentDate < deadline) {
                val dayEntries = entries.filter { it.date.isEqual(currentDate) }
                dayEntries.forEach {
                    currentBalance += it.amount
                    add(
                        Calculation(
                            date = it.date,
                            amount = it.amount,
                            balance = currentBalance,
                            name = it.name ?: it.rule!!.name,
                            type = it.type,
                            entry = it,
                            rule = null
                        )
                    )
                }

                rules.asSequence()
                    .filter { !dayEntries.any { entry -> entry.rule?.id == it.id } }
                    .filter { isDue(it, currentDate) }
                    .forEach {
                        currentBalance += it.amount
                        add(
                            Calculation(
                                date = currentDate,
                                amount = it.amount,
                                balance = currentBalance,
                                name = it.name,
                                type = it.type,
                                entry = null,
                                rule = it
                            )
                        )
                    }

                currentDate = currentDate.plusDays(1)
            }
        }
    }

    fun createEntry(): Entry = Entry(
        date = LocalDate.now(),
        amount = BigDecimal.ZERO,
        type = EntryType.REAL,
        rule = null,
        name = ""
    )

    fun saveEntry(entry: Entry): Entry = entryRepository.save(entry)

    private fun isDue(rule: Rule, currentDate: LocalDate): Boolean {
        if (rule.start > currentDate) return false
        if (rule.end != null && rule.end!! < currentDate) return false

        var ruleDate = rule.start
        while (ruleDate <= currentDate) {
            if (ruleDate.isEqual(currentDate)) return true
            ruleDate = ruleDate.plus(
                rule.schedule.interval.toLong(), when (rule.schedule.frequency) {
                    ScheduleFrequency.MONTH -> ChronoUnit.MONTHS
                    ScheduleFrequency.YEAR -> ChronoUnit.YEARS
                }
            )
        }
        return false;
    }
}

class Calculation(
    val date: LocalDate,
    val amount: BigDecimal,
    val balance: BigDecimal,
    val name: String,
    val type: EntryType,
    val entry: Entry?,
    val rule: Rule?
)