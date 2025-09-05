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

            textColumn(Calculation::date) {
                setHeader("Datum")
                isAutoWidth = true
            }
            textColumn(Calculation::amount) {
                setHeader("Betrag")
                isAutoWidth = true
            }
            textColumn(Calculation::balance) {
                setHeader("Saldo")
                isAutoWidth = true
            }
            textColumn(Calculation::name) {
                setHeader("Name")
                isAutoWidth = true
            }
            textColumn({ it.type.toDisplayString() }) {
                setHeader("Typ")
                isAutoWidth = true
            }
        }
        calculate()
    }

    private fun calculate() {
        grid.setItems(ListDataProvider(service.calculate(datePicker.value)))
    }
}