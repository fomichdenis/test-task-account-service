package com.test.task.app.controller

import com.test.task.app.entity.Account
import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.service.AccountService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException

@RequestMapping("/api/accounts")
@Controller
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping("/")
    fun createAccount(@RequestBody request: CreateAccountRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity(accountService.saveAccount(request), HttpStatus.CREATED)
        } catch (ex: ResponseStatusException) {
            ResponseEntity(ex.body ,ex.statusCode)
        }
    }

    @GetMapping("/{accountId}")
    fun getAccount(@PathVariable accountId: Long): ResponseEntity<Account> {
        val account = accountService.getAccount(accountId)
        return if (account == null) {
            ResponseEntity<Account>(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity<Account>(account, HttpStatus.OK)
        }
    }

}