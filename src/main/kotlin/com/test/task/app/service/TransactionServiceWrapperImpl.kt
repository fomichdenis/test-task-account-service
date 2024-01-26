package com.test.task.app.service

import com.test.task.app.entity.Transaction
import com.test.task.app.lock.SimpleLock
import com.test.task.app.lock.createSimpleLockForAccount
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

const val DEFAULT_LOCK_1_SECONDS = 7L
const val DEFAULT_LOCK_2_SECONDS = 5L
const val REFUND_LOCK_SECONDS = 50L

@Service
class TransactionServiceWrapperImpl(
    private val transactionService: TransactionService,
    private val updateService: TransactionStatusUpdateService
) : TransactionServiceWrapper {

    override fun transferMoney(transaction: Transaction): Transaction {
        val lock1: SimpleLock
        val lock2: SimpleLock

        // transaction.to can not be null
        if (transaction.from < transaction.to!!) {
            lock1 = createSimpleLockForAccount(transaction.from)
            lock2 = createSimpleLockForAccount(transaction.to!!)
        } else {
            lock1 = createSimpleLockForAccount(transaction.to!!)
            lock2 = createSimpleLockForAccount(transaction.from)
        }

        return tryExecuteWithLock(lock1, DEFAULT_LOCK_1_SECONDS) {
            tryExecuteWithLock(lock2, DEFAULT_LOCK_2_SECONDS) {
                transactionService.transferMoney(transaction)
            }
        }
    }

    override fun withdrawalMoney(transaction: Transaction): Transaction {
        val lock = createSimpleLockForAccount(transaction.from)
        val result = tryExecuteWithLock(lock, DEFAULT_LOCK_1_SECONDS) {
            transactionService.withdrawalMoney(transaction)
        }
        updateService.updateStatus(result.txId!!, result.from)
        return result
    }

    override fun getTransactionById(txId: String, account: Long): Transaction {
        return transactionService.getTransaction(txId, account)
    }

}

fun tryExecuteWithLock(lock: SimpleLock, lockTimeout: Long, callable: Callable<Transaction>): Transaction {
    if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
        try {
            return callable.call()
        } finally {
            lock.unlock()
        }
    } else {
        throw ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "Sorry, one of requested accounts is busy"
        )
    }
}