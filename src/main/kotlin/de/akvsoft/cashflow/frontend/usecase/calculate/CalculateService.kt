package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.BalanceRepository
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryRepository
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.Rule
import de.akvsoft.cashflow.backend.database.RuleRepository
import de.akvsoft.cashflow.backend.database.ScheduleFrequency
import de.akvsoft.cashflow.frontend.util.formatDate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
@VaadinSessionScope
class CalculateService(
    private val entryRepository: EntryRepository,
    private val ruleRepository: RuleRepository,
    private val balanceRepository: BalanceRepository
) {

    fun calculate(deadline: LocalDate): List<Row> {
        val balance = balanceRepository.findFirstByOrderByMonthDesc() ?: return emptyList()

        val startDate = balance.month.withDayOfMonth(1)
        if (deadline < startDate) return emptyList()

        val entries = entryRepository.findAllByOrderByDateAsc()
        val rules = ruleRepository.findAll()

        return buildList {
            var date = startDate
            var balance = balance.balance
            while (date <= deadline) {
                if (date.dayOfMonth == 1) {
                    add(MonthHeader(YearMonth.from(date), balance))
                }

                val dayEntries = entries.filter { it.date.isEqual(date) }
                dayEntries.forEach {
                    balance += it.amount
                    add(
                        Calculation(
                            date = it.date,
                            amount = it.amount,
                            balance = balance,
                            name = it.name ?: it.rule!!.name,
                            type = it.type,
                            entry = it,
                            rule = it.rule
                        )
                    )
                }

                rules.asSequence()
                    .filter { !dayEntries.any { entry -> entry.rule?.id == it.id } }
                    .filter { isDue(it, date) }
                    .forEach {
                        balance += it.amount
                        add(
                            Calculation(
                                date = date,
                                amount = it.amount,
                                balance = balance,
                                name = it.name,
                                type = it.type,
                                entry = null,
                                rule = it
                            )
                        )
                    }

                date = date.plusDays(1)
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

sealed interface Row {
    val formattedDate: String
    val amount: BigDecimal?
    val balance: BigDecimal
    val name: String
    val type: EntryType?
}

class MonthHeader(
    month: YearMonth,
    override val balance: BigDecimal
) : Row {
    override val formattedDate = month.formatDate()
    override val amount: BigDecimal? = null
    override val name: String = ""
    override val type: EntryType? = null
}

class Calculation(
    val date: LocalDate,
    override val amount: BigDecimal,
    override val balance: BigDecimal,
    override val name: String,
    override val type: EntryType,
    val entry: Entry?,
    val rule: Rule?
) : Row {
    override val formattedDate = date.formatDate()
}