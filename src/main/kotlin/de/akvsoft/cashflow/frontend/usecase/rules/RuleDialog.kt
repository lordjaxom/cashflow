package de.akvsoft.cashflow.frontend.usecase.rules

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.validator.IntegerRangeValidator
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.Rule
import de.akvsoft.cashflow.backend.database.ScheduleFrequency
import de.akvsoft.cashflow.backend.database.toDisplayString
import java.math.BigDecimal
import java.time.LocalDate

class RuleDialog(
    private val service: RulesService,
    private val onSaved: (Rule) -> Unit
) : Dialog() {

    private val binder = Binder(Rule::class.java)

    private val name = TextField("Name").apply { isRequiredIndicatorVisible = true }
    private val type = ComboBox<EntryType>("Typ").apply {
        setItems(ListDataProvider(EntryType.entries))
        setItemLabelGenerator { it.toDisplayString() }
        isRequiredIndicatorVisible = true
    }
    private val amount = BigDecimalField("Betrag").apply {
        isRequiredIndicatorVisible = true
        value = BigDecimal.ZERO
    }
    private val start = DatePicker("Start").apply {
        isRequiredIndicatorVisible = true
        value = LocalDate.now()
    }
    private val end = DatePicker("Ende")

    // Schedule fields
    private val frequency = ComboBox<ScheduleFrequency>("Häufigkeit").apply {
        setItems(ListDataProvider(ScheduleFrequency.entries))
        setItemLabelGenerator { it.toDisplayString() }
        isRequiredIndicatorVisible = true
    }
    private val interval = IntegerField("Intervall").apply {
        min = 1; value = 1
        helperText = "1 = jeden, 2 = alle 2., ..."
    }
    private val dayOfMonth = IntegerField("Tag im Monat").apply {
        min = 1; max = 31; value = 1
        helperText = "1-31 (empfohlen: 1-28)"
    }

    private var current: Rule? = null

    init {
        headerTitle = "Regel"

        // Form
        val form = FormLayout().apply {
            add(name, type, amount, start, end, frequency, interval, dayOfMonth)
            setResponsiveSteps(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("600px", 2)
            )
            setColspan(name, 2)
        }
        add(form)

        // Binder bindings (use property names incl. nested)
        binder.forField(name).asRequired("Name ist erforderlich").bind("name")
        binder.forField(type).asRequired("Typ ist erforderlich").bind("type")
        binder.forField(amount).asRequired("Betrag ist erforderlich").bind("amount")
        binder.forField(start).asRequired("Start ist erforderlich").bind("startDate")
        binder.forField(end)/*.withValidator({ endValue, _ ->
            val s = start.value
            endValue == null || s == null || !endValue.isBefore(s)
        }, "Ende darf nicht vor Start liegen")*/.bind("endDate")

        binder.forField(frequency).asRequired("Häufigkeit ist erforderlich").bind("schedule.frequency")
        binder.forField(interval)
            .withValidator(IntegerRangeValidator("Intervall muss >= 1 sein", 1, null))
            .bind("schedule.internal")
        binder.forField(dayOfMonth)
            .withValidator(IntegerRangeValidator("Tag muss zwischen 1 und 31 sein", 1, 31))
            .bind("schedule.dayOfMonth")

        start.addValueChangeListener { binder.validate() }

        // Footer with actions
        val cancel = Button("Abbrechen") { close() }
        val save = Button("Speichern") {
            try {
                val bean = current ?: return@Button
                binder.writeBean(bean)
                val saved = service.save(bean)
                onSaved(saved)
                close()
            } catch (ex: ValidationException) {
                // binder shows errors; do nothing
            }
        }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }

        footer.add(HorizontalLayout(cancel, save))
        isModal = true
        isDraggable = true
        isResizable = true
        width = "640px"
    }

    fun open(rule: Rule) {
        this.current = rule
        binder.readBean(rule)
        open()
    }
}
