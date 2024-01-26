package com.test.task.app.service

import com.test.task.app.entity.TransactionStatus
import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.entitymanager.AccountEntityManagerImpl
import com.test.task.app.repository.AccountRepository
import com.test.task.app.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class TransactionServiceWrapperTest {

   @Autowired
   private lateinit var transactionRepository: TransactionRepository
   @Autowired
   private lateinit var accountRepository: AccountRepository

   private lateinit var accountService: AccountService
   private lateinit var transactionService: TransactionServiceWrapper


   @BeforeEach
   fun setUp() {
      val accountEntityManager = AccountEntityManagerImpl(accountRepository)
      accountService = AccountServiceImpl(accountEntityManager)
      accountRepository.deleteAll()

      val service = TransactionServiceImpl(
         transactionRepository = transactionRepository,
         accountEntityManager = accountEntityManager
      )

      transactionService = TransactionServiceWrapperImpl(
         service,
         TransactionStatusUpdateServiceImpl(service)
      )

      transactionRepository.deleteAll()

   }

   @Test
   fun checkWithdrawalFinishedCorrectlyMultipleTimes() {
      val account = accountService.saveAccount(CreateAccountRequest(BigDecimal(1500)))
      for (j in 1..10) {
         if (checkWithdrawalFinishedWithRefund(account.accountId!!)) {
            break
         }
      }
   }

   private fun checkWithdrawalFinishedWithRefund(accountId: Long): Boolean {
      val initialAccount = accountService.getAccount(accountId)
      assertNotNull(initialAccount)
      val transaction = transactionService.withdrawalMoney(createWithdrawalTransaction(accountId))
      assertNotNull(transaction.txId)
      assertEquals(TransactionStatus.PROCESSING, transaction.status)
      val updatedAccount = accountService.getAccount(accountId)
      assertNotNull(updatedAccount)
      assertTrue { updatedAccount.balance.plus(BigDecimal(10)).minus(initialAccount.balance).abs() < BigDecimal(0.01) }

      Thread.sleep(15000)
      val updatedTransaction = transactionService.getTransactionById(transaction.txId!!, transaction.from)
      assertFalse {  updatedTransaction.status == TransactionStatus.PROCESSING }

      if (updatedTransaction.status == TransactionStatus.FAILED) {
         val refundedAccount = accountService.getAccount(accountId)
         assertNotNull(refundedAccount)
         assertTrue { refundedAccount.balance.minus(initialAccount.balance).abs() < BigDecimal(0.01) }
         return true
      }
      return false
   }
}