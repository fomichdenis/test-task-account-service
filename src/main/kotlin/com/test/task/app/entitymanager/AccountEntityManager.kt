package com.test.task.app.entitymanager

import com.test.task.app.entity.Account

interface AccountEntityManager {
    fun saveAccount(account: Account): Account
    fun getAccount(accountId: Long): Account?
}