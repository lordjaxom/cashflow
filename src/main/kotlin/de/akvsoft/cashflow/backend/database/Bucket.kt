package de.akvsoft.cashflow.backend.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Bucket(
    @Column(nullable = false, unique = true)
    var name: String,

    @Id
    val id: UUID = UUID.randomUUID()
)
