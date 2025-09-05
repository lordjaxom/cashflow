package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.frontend.components.button
import de.akvsoft.cashflow.frontend.components.datePicker
import de.akvsoft.cashflow.frontend.components.grid
import de.akvsoft.cashflow.frontend.components.grid.textColumn
import de.akvsoft.cashflow.frontend.components.horizontalLayout
import de.akvsoft.cashflow.frontend.components.nativeLabel

@Route("calculate")
class CalculateView : VerticalLayout() {

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
            datePicker()
            button("Berechnen")
            button("Hinzufügen")
        }
        grid<Calculation> {
            emptyStateText="Keine Einträge vorhanden"
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)

            textColumn(Calculation::date) {
                setHeader("Datum")
                isAutoWidth = true
            }
            textColumn(Calculation::amount){
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
            textColumn(Calculation::type) {
                setHeader("Typ")
                isAutoWidth = true
            }
        }
    }
}