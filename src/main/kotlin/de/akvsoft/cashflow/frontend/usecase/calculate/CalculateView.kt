package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.toDisplayString
import de.akvsoft.cashflow.frontend.components.button
import de.akvsoft.cashflow.frontend.components.datePicker
import de.akvsoft.cashflow.frontend.components.div
import de.akvsoft.cashflow.frontend.components.grid
import de.akvsoft.cashflow.frontend.components.grid.componentColumn
import de.akvsoft.cashflow.frontend.components.grid.textColumn
import de.akvsoft.cashflow.frontend.components.horizontalLayout
import de.akvsoft.cashflow.frontend.components.root
import de.akvsoft.cashflow.frontend.components.span
import de.akvsoft.cashflow.frontend.components.text
import de.akvsoft.cashflow.frontend.util.formatCurrency
import de.akvsoft.cashflow.frontend.util.formatDate
import java.math.BigDecimal
import java.time.LocalDate


@Route("calculate")
class CalculateView(
    private val service: CalculateService
) : VerticalLayout() {

    private val datePicker: DatePicker
    private val dateText: Text
    private val balanceText: Text
    private val grid: Grid<Row>

    init {
        setHeightFull()
        width = "1170px"
        style.setMargin("0 auto")

        horizontalLayout {
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.END
            setWidthFull()

            span("Stichtag:") {
                style.setFontWeight(Style.FontWeight.BOLD)
            }
            datePicker = datePicker {
                value = LocalDate.now().plusMonths(3)
                addValueChangeListener { calculate() }
            }
            button("Berechnen") {
                addClickListener { calculate() }
            }
            button("Hinzufügen") {
                addClickListener { EntryDialog { entry -> service.saveEntry(entry) }.open(service.createEntry()) }
            }
        }
        div {
            setWidthFull()
            addClassNames("alert", "info")
            span {
                text("Voraussichtlicher Saldo am ")
                dateText = text("Stichtag")
                text(":")
                style.setFontWeight(Style.FontWeight.BOLD)
            }
            span {
                balanceText = text(BigDecimal.ZERO.formatCurrency())
                style.setFontSize("1.25em")
            }
        }
        grid = grid<Row> {
            emptyStateText = "Keine Einträge vorhanden"
            isDetailsVisibleOnClick = false
            setWidthFull()
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
            addItemDoubleClickListener { editEntry(it.item) }

            textColumn({ it.formattedDate }) {
                setHeader("Datum")
                width = "150px"
                flexGrow = 0
            }
            textColumn({ it.amount?.formatCurrency() }) {
                setHeader("Betrag")
                width = "120px"
                flexGrow = 0
                setPartNameGenerator {
                    if (it is Calculation) buildString {
                        append("align-end")
                        if (it.amount < BigDecimal.ZERO) append(" negative")
                    } else null
                }
            }
            textColumn({ it.balance.formatCurrency() }) {
                setHeader("Saldo")
                width = "120px"
                flexGrow = 0
                setPartNameGenerator {
                    listOfNotNull("align-end", if (it.balance < BigDecimal.ZERO) "negative" else null).joinToString(" ")
                }
            }
            textColumn({ it.name }) {
                setHeader("Name")
                flexGrow = 1
            }
            componentColumn({ it.type?.toComponent() }) {
                setHeader("Typ")
                width = "120px"
                flexGrow = 0
            }
            textColumn({ it.formatSource() }) {
                setHeader("Quelle")
                width = "120px"
                flexGrow = 0
            }

            setPartNameGenerator { if (it is MonthHeader) "header-row" else null }
        }
        calculate()
    }

    private fun calculate() {
        val items = service.calculate(datePicker.value)
        dateText.text = datePicker.value.formatDate()
        if (!items.isEmpty()) {
            balanceText.text = items.asSequence().filterIsInstance<Calculation>().last().balance.formatCurrency()
        }
        grid.setItems(ListDataProvider(items))
    }

    private fun editEntry(item: Row) {
        if (item !is Calculation) return;
        val dialog = EntryDialog { service.saveEntry(it); calculate() }
        if (item.rule != null && item.entry == null) {
            dialog.openForRule(item.rule, item.date)
        } else {
            dialog.open(item.entry!!)
        }
    }

    private fun EntryType.toComponent() = root {
        span(toDisplayString()) {
            element.themeList += listOf(
                "badge", "pill", "small",
                when (this@toComponent) {
                    EntryType.REAL -> "success"
                    EntryType.FLATRATE -> "contrast"
                    EntryType.ESTIMATE -> "warning"
                }
            )
            element.setAttribute("aria-label", toDisplayString())
        }
    }

    private fun Row.formatSource() = when {
        this !is Calculation -> ""
        entry != null && rule != null -> "Override"
        entry != null -> "Eintrag"
        else -> "Regel"
    }
}