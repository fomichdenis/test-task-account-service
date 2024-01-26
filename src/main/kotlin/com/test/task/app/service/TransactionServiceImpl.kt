package com.test.task.app.service

import com.test.task.app.entity.Account
import com.test.task.app.entity.Transaction
import com.test.task.app.entity.TransactionStatus
import com.test.task.app.entity.TransactionType
import com.test.task.app.entitymanager.AccountEntityManager
import com.test.task.app.lock.createSimpleLockForAccount
import com.test.task.app.repository.TransactionRepository
import com.test.task.app.service.WithdrawalService.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

private val withdrawalService: WithdrawalService =
    WithdrawalServiceStub()

@Component
class TransactionServiceImpl(
    private val accountEntityManager: AccountEntityManager,
    private val transactionRepository: TransactionRepository,
): TransactionService {

    @Transactional
    override fun transferMoney(transaction: Transaction): Transaction {

        val toAccount = accountEntityManager.getAccount(transaction.to!!)
        toAccount ?: throwAccountNotExistException(transaction.to!!)

        val fromAccount = accountEntityManager.getAccount(transaction.from)
        fromAccount ?: throwAccountNotExistException(transaction.from)

        // fromAccount and toAccount are not null
        if (fromAccount!!.balance < transaction.amount) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "${transaction.from} account's balance is too low"
            )
        }
        return transferMoney(fromAccount = fromAccount, toAccount = toAccount!!, transaction = transaction)
    }

    @Transactional
    override fun getTransaction(txId: String, accountId: Long): Transaction {
        val transaction = transactionRepository.findById(txId).filter { it.from == accountId }.orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND, "Transaction $txId was not found for account $accountId")
        }
        if (transaction.status == TransactionStatus.PROCESSING
              && transaction.type == TransactionType.WITHDRAWAL
              && transaction.withdrawalId != null) {

            val requestState = withdrawalService.getRequestState(WithdrawalId(transaction.withdrawalId))
            if (requestState == WithdrawalState.FAILED) {
                transaction.status = TransactionStatus.FAILED
            } else if (requestState == WithdrawalState.COMPLETED) {
                transaction.status = TransactionStatus.COMPLETED
            }
        }
        return transaction
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun refundTransactionToAccount(transaction: Transaction): Transaction {
        val account = accountEntityManager.getAccount(transaction.from)
        if (transaction.status != TransactionStatus.PROCESSING) {
            return transaction
        }
        account?.let {
            it.balance = it.balance.plus(transaction.amount)
            accountEntityManager.saveAccount(it)
        }
        transaction.status = TransactionStatus.FAILED
        return transactionRepository.save(transaction)
    }

    @Transactional
    override fun updateTransactionStatusWithRefundIfNeeded(txId: String, accountId: Long) {
        val transaction = transactionRepository.findById(txId).filter { it.from == accountId }.orElseThrow {
            IllegalStateException("Transaction $txId was not found for account $accountId")
        }
        if (transaction.status == TransactionStatus.PROCESSING
            && transaction.type == TransactionType.WITHDRAWAL
            && transaction.withdrawalId != null) {

            val requestState = withdrawalService.getRequestState(WithdrawalId(transaction.withdrawalId))
            if (requestState == WithdrawalState.FAILED) {
                refundTransaction(transaction)
            } else if (requestState == WithdrawalState.COMPLETED) {
                transaction.status = TransactionStatus.COMPLETED
                transactionRepository.save(transaction)
            }
        }
    }

    private fun refundTransaction(transaction: Transaction): Transaction {
        val lock = createSimpleLockForAccount(transaction.from)
        val result = tryExecuteWithLock(lock, REFUND_LOCK_SECONDS) {
            refundTransactionToAccount(transaction)
        }
        return result
    }

    @Transactional
    override fun withdrawalMoney(transaction: Transaction): Transaction {
        val fromAccount = accountEntityManager.getAccount(transaction.from)
        fromAccount ?: throwAccountNotExistException(transaction.from)
        if (fromAccount!!.balance < transaction.amount) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "${transaction.from} account's balance is too low"
            )
        }
        withdrawalMoneyInt(fromAccount, transaction, 3)

        return transaction
    }

    private fun withdrawalMoneyInt(fromAccount: Account, transaction: Transaction, retryAmount: Int) {
        val withdrawalUuid = UUID.randomUUID()
        val withdrawalId = WithdrawalId(withdrawalUuid)
        val withdrawalAddress = Address(transaction.address)
        try {
            withdrawalService.requestWithdrawal(
                withdrawalId,
                withdrawalAddress,
                transaction.amount
            )
            transaction.withdrawalId = withdrawalUuid
            fromAccount.balance = fromAccount.balance.minus(transaction.amount)
            transactionRepository.save(transaction)
            accountEntityManager.saveAccount(fromAccount)
        } catch (ex: IllegalStateException) {
            if (retryAmount > 0) {
                withdrawalMoneyInt(fromAccount, transaction, retryAmount - 1)
            } else {
                throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error. Try again later."
                )
            }
        }
    }

    private fun transferMoney(fromAccount: Account, toAccount: Account, transaction: Transaction): Transaction {
        fromAccount.balance = fromAccount.balance.minus(transaction.amount)
        toAccount.balance = toAccount.balance.plus(transaction.amount)
        val result = markTransactionAsSuccessful(transaction)
        accountEntityManager.saveAccount(fromAccount)
        accountEntityManager.saveAccount(toAccount)
        return result
    }

    private fun throwAccountNotExistException(accountId: Long){
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Account $accountId doesn't exist"
        )
    }

    private fun markTransactionAsSuccessful(transaction: Transaction): Transaction {
        transaction.status = TransactionStatus.COMPLETED
        return transactionRepository.save(transaction)
    }
}