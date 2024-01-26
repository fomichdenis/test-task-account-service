package com.test.task.app.repository

import com.test.task.app.entity.Account
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
interface AccountRepository: CrudRepository<Account, Long> {

    fun getAccountByAccountId(accountId: Long): Account?

    fun save(account: Account): Account

}