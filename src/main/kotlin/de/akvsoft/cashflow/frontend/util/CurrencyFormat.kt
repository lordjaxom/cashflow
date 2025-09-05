package de.akvsoft.cashflow.frontend.util

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.textfield.BigDecimalField
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

fun currentLocale(): Locale = UI.getCurrent()?.locale ?: Locale.getDefault()

fun BigDecimal.formatCurrency(locale: Locale = currentLocale()): String =
    NumberFormat.getCurrencyInstance(locale).format(this)

fun BigDecimalField.applyCurrencyLocale(locale: Locale = currentLocale()) {
    setLocale(locale)
    val fmt = NumberFormat.getCurrencyInstance(locale)
    val symbol = runCatching { fmt.currency?.getSymbol(locale) ?: Currency.getInstance(locale).getSymbol(locale) }
        .getOrDefault("")
    val probe = fmt.format(1234)
    val trimmed = probe.replace("\u00A0", " ").trim()
    val symbolFirst = trimmed.startsWith(symbol)
    val symbolLast = trimmed.endsWith(symbol)
    prefixComponent = null
    suffixComponent = null
    if (symbolFirst) {
        prefixComponent = Span("$symbol ")
    } else if (symbolLast) {
        suffixComponent = Span(" $symbol")
    } else {
        // Fallback
        prefixComponent = Span("$symbol ")
    }
}
