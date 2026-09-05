package de.akvsoft.cashflow.frontend.usecase.calculate

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.ValidationException
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.validator.DateRangeValidator
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.backend.database.Rule
import de.akvsoft.cashflow.backend.database.toDisplayString
import java.math.BigDecimal
import java.time.LocalDate

class EntryDialog(
    private val onSaved: (Entry) -> Unit
) : Dialog() {

    private val binder = Binder(Entry::class.java)

    private val name = TextField("Name").apply {
        isRequiredIndicatorVisible = true
    }
    private val ruleDate = DatePicker("Urspr. Datum").apply {
        isReadOnly = true
        isVisible = false
    }
    private val date = DatePicker("Datum").apply {
        isRequiredIndicatorVisible = true
        value = LocalDate.now()
    }
    private val amount = BigDecimalField("Betrag").apply {
        isRequiredIndicatorVisible = true
        value = BigDecimal.ZERO
    }
    private val type = ComboBox<EntryType>("Typ").apply {
        setItems(ListDataProvider(EntryType.entries))
        setItemLabelGenerator { it.toDisplayString() }
        isRequiredIndicatorVisible = true
    }

    private var current: Entry? = null

    init {
        headerTitle = "Eintrag"

        val form = FormLayout().apply {
            add(name, ruleDate, date, amount, type)
            setResponsiveSteps(
                FormLayout.ResponsiveStep("0", 1),
                FormLayout.ResponsiveStep("600px", 2)
            )
            setColspan(name, 2)
            setColspan(ruleDate, 2)
        }
        add(form)

        binder.forField(name)
            .asRequired("Name ist erforderlich")
            .bind("name")
        binder.forField(date)
            .asRequired("Datum ist erforderlich")
            .withValidator(DateRangeValidator("Datum darf nicht in der fernen Vergangenheit liegen", LocalDate.of(1970, 1, 1), null))
            .bind("date")
        binder.forField(amount)
            .asRequired("Betrag ist erforderlich")
            .bind("amount")
        binder.forField(type)
            .asRequired("Typ ist erforderlich")
            .bind("type")

        date.addValueChangeListener { updateRuleDateVisibility() }

        val cancel = Button("Abbrechen") { close() }
        val save = Button("Speichern") {
            try {
                val bean = current ?: return@Button
                binder.writeBean(bean)
                bean.rule?.apply { bean.name = name }
                onSaved(bean)
                close()
            } catch (_: ValidationException) {
                // Fehler werden am Formular angezeigt
            }
        }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }

        footer.add(HorizontalLayout(cancel, save))
        isModal = true
        isDraggable = true
        isResizable = true
        width = "540px"
    }

    fun open(entry: Entry) {
        this.current = entry
        binder.readBean(entry)
        configureRuleFields(entry)
        open()
    }

    fun openForRule(rule: Rule, onDate: LocalDate) {
        val entry = Entry(
            date = onDate,
            amount = rule.amount,
            type = rule.type,
            rule = rule,
            name = rule.name,
            ruleDate = onDate
        )
        this.current = entry
        binder.readBean(entry)
        configureRuleFields(entry)
        open()
    }

    private fun configureRuleFields(entry: Entry) {
        val originalRuleDate = entry.ruleDate
        if (entry.rule == null) {
            name.isReadOnly = false
            ruleDate.isVisible = false
            ruleDate.clear()
            return
        }

        name.value = entry.rule!!.name
        name.isReadOnly = true
        ruleDate.value = originalRuleDate
        updateRuleDateVisibility()
    }

    private fun updateRuleDateVisibility() {
        val entry = current ?: return
        ruleDate.isVisible = entry.rule != null && ruleDate.value != null && ruleDate.value != date.value
    }
}
