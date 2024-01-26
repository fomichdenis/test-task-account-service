package com.test.task.app.service

import com.test.task.app.entity.Account
import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.entitymanager.AccountEntityManager
import com.test.task.app.mapper.createAccountFromRequest
import org.springframework.stereotype.Service


@Service
class AccountServiceImpl(
    private val accountEntityManager: AccountEntityManager
): AccountService {

    override fun saveAccount(request: CreateAccountRequest): Account {
        val account = createAccountFromRequest(request)
        return accountEntityManager.saveAccount(account)
    }

    override fun getAccount(accountId: Long): Account? {
        return accountEntityManager.getAccount(accountId)
    }

}