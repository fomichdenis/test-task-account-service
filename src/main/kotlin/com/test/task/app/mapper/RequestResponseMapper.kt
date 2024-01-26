package com.test.task.app.mapper

import com.test.task.app.entity.Account
import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.entity.requests.CreateTransactionRequest
import com.test.task.app.entity.Transaction
import com.test.task.app.entity.TransactionStatus
import com.test.task.app.entity.TransactionType
import com.test.task.app.entity.requests.WithdrawalRequest
import com.test.task.app.entity.response.TransactionResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

fun createAccountFromRequest(request: CreateAccountRequest): Account {
    validateAccount(request)
    return Account(accountId = null, balance = request.balance ?: BigDecimal(0))
}


private fun validateAccount(request: CreateAccountRequest){
    if (BigDecimal(0) > (request.balance ?: BigDecimal(0))) {
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Balance can not be lower 0"
        )
    }
}

fun createTransactionFromRequest(request: CreateTransactionRequest): Transaction {
    validateCreateTransactionRequest(request)
    return Transaction(
        txId = null,
        amount = request.amount!!,
        to = request.to!!,
        from = request.from!!,
        type = TransactionType.TRANSFER,
        status = TransactionStatus.PROCESSING,
        address = null,
        createdAt = Date(),
    )
}

fun createTransactionFromRequest(request: WithdrawalRequest): Transaction {
    validateWithdrawalRequest(request)
    return Transaction(
        txId = null,
        amount = request.amount!!,
        to = null,
        from = request.account!!,
        type = TransactionType.WITHDRAWAL,
        status = TransactionStatus.PROCESSING,
        address = request.address,
        createdAt = Date(),
    )
}

private fun validateWithdrawalRequest(request: WithdrawalRequest) {
    val nullFields = ArrayList<String>()
    if (request.account == null) {
        nullFields.add("account")
    }
    if (request.amount == null) {
        nullFields.add("amount")
    }
    if (request.address == null) {
        nullFields.add("address")
    }
    if (nullFields.isNotEmpty()){
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Fields $nullFields can not be null"
        )
    }
}


private fun validateCreateTransactionRequest(request: CreateTransactionRequest) {
    val nullFields = ArrayList<String>()
    if (request.to == null) {
        nullFields.add("to")
    }
    if (request.from == null) {
        nullFields.add("from")
    }
    if (request.amount == null) {
        nullFields.add("amount")
    }
    if (request.to == request.from) {
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "It is not possible to transfer money to the same account"
        )
    }
    if (nullFields.isNotEmpty()){
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Fields $nullFields can not be null"
        )
    }
    if (BigDecimal(0) > (request.amount)) {
        throw ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Transferred amount can not be lower 0"
        )
    }
}

fun mapTransactionToResponse(transaction: Transaction): TransactionResponse {
    return TransactionResponse(
        transactionId = transaction.txId,
        amount = transaction.amount,
        address = transaction.address,
        from = transaction.from,
        to = transaction.to,
        status = transaction.status,
        type = transaction.type,
        createdAt = transaction.createdAt
    )
}