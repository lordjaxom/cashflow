@file:OptIn(ExperimentalContracts::class)

package de.akvsoft.cashflow.frontend.components.grid

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import de.akvsoft.cashflow.frontend.components.VaadinDsl
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@VaadinDsl
fun <T> Grid<T>.componentColumn(
    componentProvider: (T) -> Component,
    block: (@VaadinDsl Grid.Column<T>).() -> Unit = {}
): Grid.Column<T> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return addComponentColumn(componentProvider).apply { isSortable = false; block() }
}

@VaadinDsl
fun <T, V> Grid<T>.textColumn(
    valueProvider: (T) -> V,
    block: (@VaadinDsl Grid.Column<T>).() -> Unit = {}
): Grid.Column<T> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return addColumn(valueProvider).apply { isSortable = false; block() }
}
