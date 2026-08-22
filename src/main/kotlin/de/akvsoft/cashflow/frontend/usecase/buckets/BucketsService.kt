package de.akvsoft.cashflow.frontend.usecase.buckets

import com.vaadin.flow.spring.annotation.VaadinSessionScope
import de.akvsoft.cashflow.backend.database.Bucket
import de.akvsoft.cashflow.backend.database.BucketRepository
import de.akvsoft.cashflow.backend.database.BucketTransaction
import de.akvsoft.cashflow.backend.database.BucketTransactionRepository
import de.akvsoft.cashflow.backend.database.BucketTransactionType
import de.akvsoft.cashflow.backend.database.Entry
import de.akvsoft.cashflow.backend.database.EntryRepository
import de.akvsoft.cashflow.backend.database.EntryType
import de.akvsoft.cashflow.frontend.usecase.calculate.CalculateService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
@VaadinSessionScope
class BucketsService(
    private val bucketRepository: BucketRepository,
    private val transactionRepository: BucketTransactionRepository,
    private val entryRepository: EntryRepository,
    private val calculateService: CalculateService
) {

    fun findAll(): List<BucketSummary> =
        bucketRepository.findAllByOrderByNameAsc()
            .map { BucketSummary(it, balance(it)) }

    fun findTransactions(bucket: Bucket): List<BucketTransaction> =
        transactionRepository.findAllByBucketOrderByDateAsc(bucket)

    fun availableBalance(): BigDecimal =
        calculateService.balanceAt(LocalDate.now()) ?: BigDecimal.ZERO

    @Transactional
    fun createBucket(name: String): Bucket {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Name ist erforderlich." }
        require(!bucketRepository.existsByNameIgnoreCase(trimmedName)) { "Ein Bucket mit diesem Namen existiert bereits." }
        return bucketRepository.save(Bucket(trimmedName))
    }

    @Transactional
    fun deleteBucket(bucket: Bucket) {
        val balance = balance(bucket)
        require(balance.compareTo(BigDecimal.ZERO) == 0) { "Nur leere Buckets können gelöscht werden." }

        transactionRepository.findAllByBucketOrderByDateAsc(bucket)
            .map { it.projectionEntryId }
            .forEach { entryRepository.findById(it).ifPresent(entryRepository::delete) }
        transactionRepository.deleteAllByBucket(bucket)
        bucketRepository.delete(bucket)
    }

    @Transactional
    fun addMoney(bucket: Bucket, date: LocalDate, amount: BigDecimal) {
        requirePositive(amount)
        require(amount <= availableBalance()) { "Der Betrag überschreitet den verfügbaren Saldo." }
        createTransaction(bucket, date, amount, BucketTransactionType.ADD)
    }

    @Transactional
    fun removeMoney(bucket: Bucket, date: LocalDate, amount: BigDecimal) {
        requirePositive(amount)
        require(amount <= balance(bucket)) { "Der Betrag überschreitet den Bucket-Saldo." }
        createTransaction(bucket, date, amount.negate(), BucketTransactionType.REMOVE)
    }

    private fun createTransaction(
        bucket: Bucket,
        date: LocalDate,
        bucketAmount: BigDecimal,
        type: BucketTransactionType
    ) {
        val entry = entryRepository.save(
            Entry(
                date = date,
                amount = bucketAmount.negate(),
                type = EntryType.REAL,
                rule = null,
                name = when (type) {
                    BucketTransactionType.ADD -> "Parken: ${bucket.name}"
                    BucketTransactionType.REMOVE -> "Entparken: ${bucket.name}"
                },
                locked = true
            )
        )
        transactionRepository.save(
            BucketTransaction(
                bucket = bucket,
                date = date,
                amount = bucketAmount,
                type = type,
                projectionEntryId = entry.id
            )
        )
    }

    private fun requirePositive(amount: BigDecimal) {
        require(amount > BigDecimal.ZERO) { "Der Betrag muss größer als 0 sein." }
    }

    private fun balance(bucket: Bucket): BigDecimal =
        transactionRepository.findAllByBucketOrderByDateAsc(bucket)
            .fold(BigDecimal.ZERO) { sum, transaction -> sum + transaction.amount }
}

class BucketSummary(
    val bucket: Bucket,
    val balance: BigDecimal
)
