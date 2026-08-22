package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.backend.database.Entry
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
import de.akvsoft.cashflow.frontend.components.verticalLayout
import de.akvsoft.cashflow.frontend.util.formatCurrency
import de.akvsoft.cashflow.frontend.util.formatDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth


@Route("calculate")
class CalculateView(
    private val service: CalculateService
) : VerticalLayout() {

    private val datePicker: DatePicker
    private val dateText: Text
    private val balanceText: Text
    private val grid: Grid<Row>
    private val deleteButton: Button
    private val squashButton: Button
    private var rows: List<Row> = emptyList()

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
                value = service.loadDeadline()
                addValueChangeListener {
                    val deadline = it.value ?: return@addValueChangeListener
                    service.saveDeadline(deadline)
                    calculate()
                }
            }
            button("Berechnen") {
                addClickListener {
                    val deadline = datePicker.value ?: return@addClickListener
                    service.saveDeadline(deadline)
                    calculate()
                }
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
        verticalLayout {
            isPadding = false
            style.setGap("0")
            setSizeFull()

            horizontalLayout {
                alignItems = FlexComponent.Alignment.CENTER
                justifyContentMode = FlexComponent.JustifyContentMode.END
                setWidthFull()

                deleteButton = button("Löschen") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    isEnabled = false
                    addClickListener { deleteEntry() }
                }
                squashButton = button("Verdichten") {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    isEnabled = false
                    style.setMarginInlineEnd("auto")
                    addClickListener { confirmSquash() }
                }
                button("Hinzufügen") {
                    addClickListener { EntryDialog { entry -> service.saveEntry(entry) }.open(service.createEntry()) }
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

                setPartNameGenerator {
                    if (it is MonthHeader) {
                        listOfNotNull(
                            "header-row",
                            if (selectedItems.contains(it)) "selected-header-row" else null
                        ).joinToString(" ")
                    } else null
                }
                addSelectionListener {
                    deleteButton.isEnabled = selectedCalculation()?.deletableEntry() != null
                    squashButton.isEnabled = selectedMonthHeader()?.squashableMonth() != null
                    dataProvider.refreshAll()
                }
            }
        }

        calculate()
    }

    private fun calculate() {
        val deadline = datePicker.value ?: return
        val items = service.calculate(deadline)
        rows = items
        dateText.text = deadline.formatDate()
        if (items.isNotEmpty()) {
            balanceText.text = items.asSequence().filterIsInstance<Calculation>().last().balance.formatCurrency()
        }
        grid.setItems(ListDataProvider(items))
    }

    private fun editEntry(item: Row) {
        if (item !is Calculation) return;
        if (item.entry?.locked == true) return
        val dialog = EntryDialog { service.saveEntry(it); calculate() }
        if (item.rule != null && item.entry == null) {
            dialog.openForRule(item.rule, item.date)
        } else {
            dialog.open(item.entry!!)
        }
    }

    private fun deleteEntry() {
        val entry = selectedCalculation()?.deletableEntry() ?: return
        service.deleteEntry(entry)
        calculate()
    }

    private fun confirmSquash() {
        val month = selectedMonthHeader()?.squashableMonth() ?: return
        val anchor = month.atDay(1)
        val balance = service.balanceAtStartOfMonth(month) ?: return

        ConfirmDialog(
            "Projektion verdichten",
            "Der Saldo per ${anchor.formatDate()} (${balance.formatCurrency()}) wird gespeichert. " +
                "Alle Einträge vor diesem Datum werden dauerhaft gelöscht.",
            "Abschließen",
            {
                service.squash(month)
                calculate()
            },
            "Abbrechen",
            {}
        ).apply {
            setConfirmButtonTheme("error primary")
            open()
        }
    }

    private fun selectedCalculation(): Calculation? = grid.selectedItems.firstOrNull() as? Calculation

    private fun selectedMonthHeader(): MonthHeader? = grid.selectedItems.firstOrNull() as? MonthHeader

    private fun Calculation.deletableEntry() =
        entry?.takeIf { !it.locked }

    private fun MonthHeader.squashableMonth() =
        if (this != rows.firstOrNull()) month else null

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
        entry?.locked == true -> "Bucket"
        entry != null && rule != null -> "Override"
        entry != null -> "Eintrag"
        else -> "Regel"
    }
}
