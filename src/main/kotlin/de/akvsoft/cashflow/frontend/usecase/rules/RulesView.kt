package de.akvsoft.cashflow.frontend.usecase.rules

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.backend.database.Rule
import de.akvsoft.cashflow.backend.database.toDisplayString
import de.akvsoft.cashflow.frontend.components.button
import de.akvsoft.cashflow.frontend.components.grid
import de.akvsoft.cashflow.frontend.components.grid.textColumn
import de.akvsoft.cashflow.frontend.components.horizontalLayout
import de.akvsoft.cashflow.frontend.util.formatCurrency
import de.akvsoft.cashflow.frontend.util.formatDate
import java.math.BigDecimal

@Route("rules")
class RulesView(
    private val service: RulesService
) : VerticalLayout() {

    private val ruleGrid: Grid<Rule>

    init {
        setHeightFull()
        width = "1170px"
        themeList -= "spacing"
        style.setMargin("0 auto")

        horizontalLayout {
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.END
            setWidthFull()

            button("Hinzufügen") {
                addClickListener {
                    RuleDialog(service) { reload() }.open(service.create())
                }
            }
        }
        ruleGrid = grid<Rule> {
            emptyStateText = "Keine Einträge vorhanden"
            setSizeFull()
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES)

            textColumn(Rule::name) {
                setHeader("Name")
                flexGrow = 1
            }
            textColumn( {  it.type.toDisplayString() }) {
                setHeader("Typ")
                width = "100px"
                flexGrow = 0
            }
            textColumn( { it.amount.formatCurrency() }) {
                setHeader("Betrag")
                setPartNameGenerator {
                    buildString {
                        append("align-end")
                        if (it.amount < BigDecimal.ZERO) append(" negative")
                    }
                }
                width = "120px"
                flexGrow = 0
            }
            textColumn( { it.start.formatDate()}) {
                setHeader("Beginn")
                width = "120px"
                flexGrow = 0
            }
            textColumn( { it.end?.formatDate()}) {
                setHeader("Ende")
                width = "120px"
                flexGrow = 0
            }
            textColumn({ service.scheduleLabel(it) }) {
                setHeader("Zeitplan")
                width = "300px"
                flexGrow = 0
            }

            addItemDoubleClickListener { event ->
                RuleDialog(service) { reload() }.open(event.item)
            }
        }
        reload()
    }

    private fun reload() {
        ruleGrid.setItems(service.findAll())
    }
}