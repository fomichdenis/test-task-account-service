package com.test.task.app.service

import com.test.task.app.entity.Transaction
import java.math.BigDecimal

interface TransactionService {
    fun transferMoney(transaction: Transaction): Transaction
    fun withdrawalMoney(transaction: Transaction): Transaction
    fun getTransaction(txId: String, accountId: Long): Transaction
    fun refundTransactionToAccount(transaction: Transaction): Transaction
    fun updateTransactionStatusWithRefundIfNeeded(txId: String, accountId: Long)
}