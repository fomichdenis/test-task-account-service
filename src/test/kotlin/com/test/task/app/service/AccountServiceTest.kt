package com.test.task.app.service

import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.entitymanager.AccountEntityManagerImpl
import com.test.task.app.repository.AccountRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class AccountServiceTest {

   @Autowired
   private lateinit var accountRepository: AccountRepository

   private lateinit var accountService: AccountService

   @BeforeEach
   fun setUp() {
      val accountEntityManager = AccountEntityManagerImpl(accountRepository)
      accountService = AccountServiceImpl(accountEntityManager)
      accountRepository.deleteAll()
   }

   @Test
   fun checkCreated() {
      val account = accountService.saveAccount(CreateAccountRequest(BigDecimal(100.9)))
      assertNotNull(account.accountId)
      val account1 = accountService.getAccount(accountId = account.accountId!!)
      assertNotNull(account1)
      assertEquals(account.accountId, account1.accountId)
      assertTrue { account.balance.minus(account1.balance).abs() < BigDecimal(0.01)  }
   }
}