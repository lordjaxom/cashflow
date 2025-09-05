package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.backend.database.toDisplayString
import de.akvsoft.cashflow.frontend.components.button
import de.akvsoft.cashflow.frontend.components.datePicker
import de.akvsoft.cashflow.frontend.components.grid
import de.akvsoft.cashflow.frontend.components.grid.textColumn
import de.akvsoft.cashflow.frontend.components.horizontalLayout
import de.akvsoft.cashflow.frontend.components.nativeLabel
import de.akvsoft.cashflow.frontend.util.formatCurrency
import de.akvsoft.cashflow.frontend.util.formatDate
import java.math.BigDecimal
import java.time.LocalDate

@Route("calculate")
class CalculateView(
    private val service: CalculateService
) : VerticalLayout() {

    private val datePicker: DatePicker
    private val grid: Grid<Calculation>

    init {
        setHeightFull()
        width = "1170px"
        themeList -= "spacing"
        style.setMargin("0 auto")

        horizontalLayout {
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.END
            setWidthFull()

            nativeLabel("Stichtag:")
            datePicker = datePicker {
                value = LocalDate.now().plusMonths(3)
            }
            button("Berechnen") {
                addClickListener { calculate() }
            }
            button("Hinzufügen") {
                addClickListener { EntryDialog { entry -> service.saveEntry(entry) }.open(service.createEntry()) }
            }
        }
        grid = grid<Calculation> {
            emptyStateText = "Keine Einträge vorhanden"
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)

            textColumn({ it.date.formatDate() }) {
                setHeader("Datum")
                width = "120px"
                flexGrow = 0
            }
            textColumn({ it.amount.formatCurrency() }) {
                setHeader("Betrag")
                width = "120px"
                flexGrow = 0
                setPartNameGenerator {
                    buildString {
                        append("align-end")
                        if (it.amount < BigDecimal.ZERO) append(" negative")
                    }
                }
            }
            textColumn({ it.balance.formatCurrency() }) {
                setHeader("Saldo")
                width = "120px"
                flexGrow = 0
                setPartNameGenerator {
                    buildString {
                        append("align-end")
                        if (it.balance < BigDecimal.ZERO) append(" negative")
                    }
                }
            }
            textColumn(Calculation::name) {
                setHeader("Name")
                flexGrow = 1
            }
            textColumn({ it.type.toDisplayString() }) {
                setHeader("Typ")
                width = "100px"
                flexGrow = 0
            }

            addItemDoubleClickListener { event ->
                val item = event.item
                val rule = item.rule
                if (rule != null) {
                    EntryDialog { entry ->
                        entry.rule = rule
                        service.saveEntry(entry)
                        calculate()
                    }.openForRule(rule, item.date)
                }
            }
        }
        calculate()
    }

    private fun calculate() {
        grid.setItems(ListDataProvider(service.calculate(datePicker.value)))
    }
}