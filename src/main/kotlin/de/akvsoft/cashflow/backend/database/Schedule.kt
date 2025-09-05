package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

enum class ScheduleFrequency {
    MONTH, YEAR
}

@Entity
class Schedule(
    @Column(nullable = false)
    var frequency: ScheduleFrequency,

    @Column(name="interval_", nullable = false)
    var interval: Int,

    @Column(nullable = false)
    var dayOfMonth: Int,

    @Id
    val id: UUID = UUID.randomUUID()
)

fun ScheduleFrequency.toDisplayString() = when (this) {
    ScheduleFrequency.MONTH -> "monatlich"
    ScheduleFrequency.YEAR -> "jährlich"
}