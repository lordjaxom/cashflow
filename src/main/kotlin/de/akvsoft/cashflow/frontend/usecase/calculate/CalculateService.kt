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

                addAll(buildList {
                    entries.asSequence()
                        .filter { it.date.isEqual(date) }
                        .forEach { balance += it.amount; add(it.toCalculation(balance)) }

                    rules.asSequence()
                        .filter { !any { calc -> calc is Calculation && calc.rule?.id == it.id } }
                        .filter { it.isDue(date) }
                        .forEach { balance += it.amount; add(it.toCalculation(date, balance)) }

                })

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

    fun saveEntry(entry: Entry) {
        entryRepository.save(entry)
    }

    fun deleteEntry(entry: Entry) {
        entryRepository.delete(entry)
    }

    private fun Rule.isDue(date: LocalDate): Boolean {
        if (start > date) return false
        if (end?.let { it < date } == true) return false

        var ruleDate = start
        while (ruleDate <= date) {
            if (ruleDate.isEqual(date)) return true
            ruleDate = ruleDate.plus(
                schedule.interval.toLong(), when (schedule.frequency) {
                    ScheduleFrequency.MONTH -> ChronoUnit.MONTHS
                    ScheduleFrequency.YEAR -> ChronoUnit.YEARS
                }
            )
        }
        return false;
    }

    private fun Entry.toCalculation(balance: BigDecimal) =
        Calculation(
            date = date,
            amount = amount,
            balance = balance,
            name = name ?: rule!!.name,
            type = type,
            entry = this,
            rule = rule
        )

    private fun Rule.toCalculation(date: LocalDate, balance: BigDecimal) =
        Calculation(
            date = date,
            amount = amount,
            balance = balance,
            name = name,
            type = type,
            entry = null,
            rule = this
        )
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