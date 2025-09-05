package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryRepository
import de.akvsoft.cashflow.backend.database.EntryType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
@VaadinSessionScope
class CalculateService(
    private val entryRepository: EntryRepository,
) {

    fun calculate(deadline: LocalDate): List<Calculation> {
        // Platzhalter: Implementierung folgt später
        return emptyList()
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