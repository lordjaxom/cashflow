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

    @Column(nullable = false)
    var interval: Int,

    @Column(nullable = false)
    var dayOfMonth: Int,

    @Id
    val id: UUID = UUID.randomUUID()
)