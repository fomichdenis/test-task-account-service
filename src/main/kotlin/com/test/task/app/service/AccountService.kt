package com.test.task.app.service

import com.test.task.app.entity.Account
import com.test.task.app.entity.requests.CreateAccountRequest

interface AccountService {
    fun saveAccount(request: CreateAccountRequest): Account
    fun getAccount(accountId: Long): Account?
}