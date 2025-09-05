package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryRepository
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.RuleRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

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
                addAll(dayEntries.map {
                    currentBalance += it.amount
                    Calculation(
                        date = it.date,
                        amount = it.amount,
                        balance = currentBalance,
                        name = it.name ?: it.rule!!.name,
                        type = it.type
                    )
                })
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
}

class Calculation(
    val date: LocalDate,
    val amount: BigDecimal,
    val balance: BigDecimal,
    val name: String,
    val type: EntryType,
)