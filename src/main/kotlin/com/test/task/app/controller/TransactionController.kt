package com.test.task.app.controller

import com.test.task.app.entity.requests.CreateTransactionRequest
import com.test.task.app.entity.requests.WithdrawalRequest
import com.test.task.app.mapper.createTransactionFromRequest
import com.test.task.app.mapper.mapTransactionToResponse
import com.test.task.app.service.TransactionServiceWrapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException

@RequestMapping("")
@Controller
class TransactionController(
    private val transactionServiceWrapper: TransactionServiceWrapper
) {

    @PostMapping("/api/transactions/transfer")
    fun transferMoney(@RequestBody request: CreateTransactionRequest): ResponseEntity<Any> {
        return try {
            val transaction = createTransactionFromRequest(request)
            val response = mapTransactionToResponse(transactionServiceWrapper.transferMoney(transaction))
            ResponseEntity(response, HttpStatus.CREATED)
        } catch (ex: ResponseStatusException) {
            ResponseEntity(ex.body ,ex.statusCode)
        }
    }

    @PostMapping("/api/transactions/withdrawal")
    fun withdrawalMoney(@RequestBody request: WithdrawalRequest): ResponseEntity<Any> {
        return try {
            val transaction = createTransactionFromRequest(request)
            val response = mapTransactionToResponse(transactionServiceWrapper.withdrawalMoney(transaction))
            ResponseEntity(response, HttpStatus.ACCEPTED)
        } catch (ex: ResponseStatusException) {
            ResponseEntity(ex.body ,ex.statusCode)
        }
    }

    @GetMapping("/api/accounts/{accountId}/transactions/{txId}")
    fun getTransaction(@PathVariable txId: String, @PathVariable accountId: Long): ResponseEntity<Any> {
        return try {
            val response = mapTransactionToResponse(transactionServiceWrapper.getTransactionById(txId, accountId))
            ResponseEntity(response, HttpStatus.OK)
        } catch (ex: ResponseStatusException) {
            ResponseEntity(ex.body ,ex.statusCode)
        }
    }

}