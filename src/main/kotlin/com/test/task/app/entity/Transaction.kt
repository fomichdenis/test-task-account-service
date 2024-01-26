package com.test.task.app.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "transactions")
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var txId: String?,
    var amount: BigDecimal,
    @Column(name = "from_account")
    var from: Long,
    @Column(name = "to_account")
    var to: Long?,
    var withdrawalId: UUID? = null,
    var address: String?,
    var type: TransactionType,
    var status: TransactionStatus,
    @Column(name = "created_at")
    var createdAt: Date
)

enum class TransactionType {
    WITHDRAWAL, TRANSFER
}

enum class TransactionStatus {
    COMPLETED, PROCESSING, FAILED
}