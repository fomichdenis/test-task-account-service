package com.test.task.app.entitymanager

import com.test.task.app.entity.Account
import com.test.task.app.repository.AccountRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AccountEntityManagerImpl(
    private val accountRepository: AccountRepository
) : AccountEntityManager {

    @Transactional
    override fun saveAccount(account: Account): Account {
        return accountRepository.save(account)
    }

    @Transactional(readOnly = true)
    override fun getAccount(accountId: Long): Account? {
        return accountRepository.getAccountByAccountId(accountId)
    }

}