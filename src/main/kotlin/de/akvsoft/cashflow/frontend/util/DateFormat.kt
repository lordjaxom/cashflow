package de.akvsoft.cashflow.frontend.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun LocalDate.formatDate(locale: Locale = currentLocale()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)
        .format(this)

fun YearMonth.formatDate(locale: Locale = currentLocale()): String =
    DateTimeFormatter.ofPattern("MMMM yyyy", locale)
        .withLocale(locale)
        .format(this)