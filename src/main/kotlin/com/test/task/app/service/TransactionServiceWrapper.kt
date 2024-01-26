package com.test.task.app.service

import com.test.task.app.entity.Transaction

interface TransactionServiceWrapper {
    fun transferMoney(transaction: Transaction): Transaction
    fun withdrawalMoney(transaction: Transaction): Transaction
    fun getTransactionById(txId: String, account:Long): Transaction
}