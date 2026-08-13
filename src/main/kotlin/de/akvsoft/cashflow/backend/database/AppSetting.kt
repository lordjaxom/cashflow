package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

@Entity
class AppSetting(
    @Column(name = "setting_key", nullable = false, unique = true)
    var key: String,

    @Column(name = "setting_value", nullable = false)
    var value: String,

    @Id
    val id: UUID = UUID.randomUUID()
)
