package de.akvsoft.cashflow.frontend.usecase.buckets

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.confirmdialog.ConfirmDialog
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.BigDecimalField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.dom.Style
import com.vaadin.flow.router.Route
import de.akvsoft.cashflow.backend.database.Bucket
import de.akvsoft.cashflow.backend.database.BucketTransaction
import de.akvsoft.cashflow.backend.database.toDisplayString
import de.akvsoft.cashflow.frontend.components.button
import de.akvsoft.cashflow.frontend.components.grid
import de.akvsoft.cashflow.frontend.components.grid.textColumn
import de.akvsoft.cashflow.frontend.components.horizontalLayout
import de.akvsoft.cashflow.frontend.components.span
import de.akvsoft.cashflow.frontend.components.verticalLayout
import de.akvsoft.cashflow.frontend.util.formatCurrency
import de.akvsoft.cashflow.frontend.util.formatDate
import java.math.BigDecimal
import java.time.LocalDate

@Route("buckets")
class BucketsView(
    private val service: BucketsService
) : VerticalLayout() {

    private val bucketGrid: Grid<BucketSummary>
    private val transactionGrid: Grid<BucketTransaction>
    private val bucketProvider = ListDataProvider<BucketSummary>(mutableListOf())
    private val transactionProvider = ListDataProvider<BucketTransaction>(mutableListOf())
    private val availableBalanceText: Span
    private val selectedBucketText: Span
    private val addButton: Button
    private val removeButton: Button
    private val revertButton: Button
    private val deleteButton: Button

    init {
        setHeightFull()
        width = "1170px"
        themeList -= "spacing"
        style.setMargin("0 auto")

        horizontalLayout {
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.END
            setWidthFull()

            span("Verfügbar:") {
                style.setFontWeight(Style.FontWeight.BOLD)
            }
            availableBalanceText = span()
            button("Bucket erstellen") {
                addClickListener {
                    BucketNameDialog { name ->
                        runAction {
                            service.createBucket(name)
                            reload()
                        }
                    }.open()
                }
            }
        }

        horizontalLayout {
            setSizeFull()
            isPadding = false

            verticalLayout {
                isPadding = false
                width = "40%"
                height = "100%"

                bucketGrid = grid<BucketSummary> {
                    emptyStateText = "Keine Buckets vorhanden"
                    setSizeFull()
                    addThemeVariants(GridVariant.LUMO_ROW_STRIPES)

                    textColumn({ it.bucket.name }) {
                        setHeader("Bucket")
                        flexGrow = 1
                    }
                    textColumn({ it.balance.formatCurrency() }) {
                        setHeader("Saldo")
                        width = "140px"
                        flexGrow = 0
                        setPartNameGenerator {
                            listOfNotNull("align-end", if (it.balance < BigDecimal.ZERO) "negative" else null)
                                .joinToString(" ")
                        }
                    }

                    addSelectionListener { reloadTransactions() }
                    setItems(bucketProvider)
                }
            }

            verticalLayout {
                isPadding = false
                width = "60%"
                height = "100%"

                horizontalLayout {
                    alignItems = FlexComponent.Alignment.CENTER
                    justifyContentMode = FlexComponent.JustifyContentMode.END
                    setWidthFull()

                    selectedBucketText = span("Kein Bucket ausgewählt") {
                        style.setFontWeight(Style.FontWeight.BOLD)
                        style.setMarginInlineEnd("auto")
                    }
                    addButton = button("Parken") {
                        isEnabled = false
                        addClickListener {
                            val bucket = selectedBucket() ?: return@addClickListener
                            MoneyDialog("Parken") { date, amount ->
                                runAction {
                                    service.addMoney(bucket, date, amount)
                                    reload()
                                }
                            }.open()
                        }
                    }
                    removeButton = button("Entparken") {
                        isEnabled = false
                        addClickListener {
                            val bucket = selectedBucket() ?: return@addClickListener
                            MoneyDialog("Entparken") { date, amount ->
                                runAction {
                                    service.removeMoney(bucket, date, amount)
                                    reload()
                                }
                            }.open()
                        }
                    }
                    revertButton = button("Rückgängig") {
                        isEnabled = false
                        addThemeVariants(ButtonVariant.LUMO_ERROR)
                        addClickListener { confirmRevertTransaction() }
                    }
                    deleteButton = button("Bucket löschen") {
                        isEnabled = false
                        addThemeVariants(ButtonVariant.LUMO_ERROR)
                        addClickListener { confirmDeleteBucket() }
                    }
                }

                transactionGrid = grid<BucketTransaction> {
                    emptyStateText = "Keine Transaktionen vorhanden"
                    setSizeFull()
                    addThemeVariants(GridVariant.LUMO_ROW_STRIPES)

                    textColumn({ it.date.formatDate() }) {
                        setHeader("Datum")
                        width = "130px"
                        flexGrow = 0
                    }
                    textColumn({ it.amount.formatCurrency() }) {
                        setHeader("Betrag")
                        width = "140px"
                        flexGrow = 0
                        setPartNameGenerator {
                            listOfNotNull("align-end", if (it.amount < BigDecimal.ZERO) "negative" else null)
                                .joinToString(" ")
                        }
                    }
                    textColumn({ it.type.toDisplayString() }) {
                        setHeader("Typ")
                        width = "120px"
                        flexGrow = 0
                    }

                    addSelectionListener { updateActionButtons() }
                    setItems(transactionProvider)
                }
            }
        }

        reload()
    }

    private fun reload() {
        val selectedId = selectedBucket()?.id
        bucketProvider.items.clear()
        bucketProvider.items.addAll(service.findAll())
        bucketProvider.refreshAll()
        availableBalanceText.text = service.availableBalance().formatCurrency()

        selectedId
            ?.let { id -> bucketProvider.items.firstOrNull { it.bucket.id == id } }
            ?.also { bucketGrid.select(it) }
        reloadTransactions()
    }

    private fun reloadTransactions() {
        val summary = bucketGrid.selectedItems.firstOrNull()
        val bucket = summary?.bucket
        selectedBucketText.text = bucket?.name ?: "Kein Bucket ausgewählt"
        addButton.isEnabled = bucket != null
        removeButton.isEnabled = summary?.balance?.let { it > BigDecimal.ZERO } == true
        deleteButton.isEnabled = summary?.balance?.compareTo(BigDecimal.ZERO) == 0

        transactionGrid.deselectAll()
        transactionProvider.items.clear()
        if (bucket != null) {
            transactionProvider.items.addAll(service.findTransactions(bucket))
        }
        transactionProvider.refreshAll()
        updateActionButtons()
    }

    private fun selectedBucket(): Bucket? =
        bucketGrid.selectedItems.firstOrNull()?.bucket

    private fun selectedTransaction(): BucketTransaction? =
        transactionGrid.selectedItems.firstOrNull()

    private fun updateActionButtons() {
        val summary = bucketGrid.selectedItems.firstOrNull()
        val bucket = summary?.bucket
        addButton.isEnabled = bucket != null
        removeButton.isEnabled = summary?.balance?.let { it > BigDecimal.ZERO } == true
        deleteButton.isEnabled = summary?.balance?.compareTo(BigDecimal.ZERO) == 0
        revertButton.isEnabled = selectedTransaction()?.let { service.canRevert(it) } == true
    }

    private fun confirmDeleteBucket() {
        val bucket = selectedBucket() ?: return
        ConfirmDialog(
            "Bucket löschen",
            "Der leere Bucket '${bucket.name}' und seine Transaktionen werden dauerhaft gelöscht.",
            "Löschen",
            {
                runAction {
                    service.deleteBucket(bucket)
                    reload()
                }
            },
            "Abbrechen",
            {}
        ).apply {
            setConfirmButtonTheme("error primary")
            open()
        }
    }

    private fun confirmRevertTransaction() {
        val transaction = selectedTransaction() ?: return
        if (!service.canRevert(transaction)) return

        ConfirmDialog(
            "Transaktion rückgängig machen",
            "Die letzte Transaktion über ${transaction.amount.formatCurrency()} und der zugehörige Projektionseintrag werden gelöscht.",
            "Rückgängig machen",
            {
                runAction {
                    service.revertTransaction(transaction)
                    reload()
                }
            },
            "Abbrechen",
            {}
        ).apply {
            setConfirmButtonTheme("error primary")
            open()
        }
    }

    private fun runAction(action: () -> Unit) {
        try {
            action()
        } catch (exception: IllegalArgumentException) {
            Notification.show(exception.message ?: "Aktion nicht möglich.")
        }
    }
}

private class BucketNameDialog(
    private val onSave: (String) -> Unit
) : Dialog() {

    private val name = TextField("Name").apply {
        isRequiredIndicatorVisible = true
    }

    init {
        headerTitle = "Bucket erstellen"
        add(name)
        footer.add(
            HorizontalLayout(
                Button("Abbrechen") { close() },
                Button("Speichern") {
                    onSave(name.value)
                    close()
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }
            )
        )
        isModal = true
        width = "420px"
    }
}

private class MoneyDialog(
    title: String,
    private val onSave: (LocalDate, BigDecimal) -> Unit
) : Dialog() {

    private val date = DatePicker("Datum").apply {
        isRequiredIndicatorVisible = true
        value = LocalDate.now()
    }
    private val amount = BigDecimalField("Betrag").apply {
        isRequiredIndicatorVisible = true
        value = BigDecimal.ZERO
    }

    init {
        headerTitle = title
        add(VerticalLayout(date, amount).apply { isPadding = false })
        footer.add(
            HorizontalLayout(
                Button("Abbrechen") { close() },
                Button("Speichern") {
                    val selectedDate = date.value ?: return@Button
                    val selectedAmount = amount.value ?: return@Button
                    onSave(selectedDate, selectedAmount)
                    close()
                }.apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }
            )
        )
        isModal = true
        width = "420px"
    }
}
