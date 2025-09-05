package de.akvsoft.cashflow

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CashflowApplication

fun main(args: Array<String>) {
    runApplication<CashflowApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}