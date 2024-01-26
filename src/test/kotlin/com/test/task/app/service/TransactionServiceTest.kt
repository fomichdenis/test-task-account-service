package com.test.task.app.service

import com.test.task.app.entity.Transaction
import com.test.task.app.entity.TransactionStatus
import com.test.task.app.entity.TransactionType
import com.test.task.app.entity.requests.CreateAccountRequest
import com.test.task.app.entitymanager.AccountEntityManagerImpl
import com.test.task.app.repository.AccountRepository
import com.test.task.app.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.util.*
import kotlin.test.*

@SpringBootTest
class TransactionServiceTest {

   @Autowired
   private lateinit var transactionRepository: TransactionRepository
   @Autowired
   private lateinit var accountRepository: AccountRepository

   private lateinit var accountService: AccountService
   private lateinit var transactionService: TransactionService


   @BeforeEach
   fun setUp() {
      val accountEntityManager = AccountEntityManagerImpl(accountRepository)
      accountService = AccountServiceImpl(accountEntityManager)
      accountRepository.deleteAll()

      transactionService = TransactionServiceImpl(
         transactionRepository = transactionRepository,
         accountEntityManager = accountEntityManager
      )
      transactionRepository.deleteAll()

   }

   @Test
   fun checkTransferFailsWithoutFromAccount() {
      assertFailsWith<ResponseStatusException>(
         message = "Account 1 doesn't exist"
      ) {
         transactionService.transferMoney(createTransferTransaction(1L, 2L))
      }
   }

   @Test
   fun checkTransferFailsWithoutToAccount() {
      accountService.saveAccount(CreateAccountRequest(BigDecimal(10)))
      assertFailsWith<ResponseStatusException>(
         message = "Account 2 doesn't exist"
      ) {
         transactionService.transferMoney(createTransferTransaction(1L, 2L))
      }
   }

   @Test
   fun checkTransferFailsWithLowBalanceToTransfer() {
      accountService.saveAccount(CreateAccountRequest(BigDecimal(5)))
      accountService.saveAccount(CreateAccountRequest(BigDecimal(10)))
      assertFailsWith<ResponseStatusException>(
         message = "1 account's balance is too low"
      ) {
         transactionService.transferMoney(createTransferTransaction(1L, 2L))
      }
   }

   @Test
   fun checkTransferCompleted() {
      val account1 = accountService.saveAccount(CreateAccountRequest(BigDecimal(15)))
      val account2 = accountService.saveAccount(CreateAccountRequest(BigDecimal(10)))
      val transaction = transactionService.transferMoney(createTransferTransaction(account1.accountId!!, account2.accountId!!))
      assertEquals(TransactionStatus.COMPLETED, transaction.status)
      val updatedAccount1 = accountService.getAccount(account1.accountId!!)
      assertNotNull(updatedAccount1)
      assertTrue { updatedAccount1.balance.minus(BigDecimal(5)).abs() < BigDecimal(0.01) }

      val updatedAccount2 = accountService.getAccount(account2.accountId!!)
      assertNotNull(updatedAccount2)
      assertTrue { updatedAccount2.balance.minus(BigDecimal(20)).abs() < BigDecimal(0.01) }
   }

   @Test
   fun checkWithdrawalFailedWithNoAccount() {
      assertFailsWith<ResponseStatusException>(
         message = "Account 1 doesn't exist"
      ) {
         transactionService.withdrawalMoney(createWithdrawalTransaction(1L))
      }
   }

   @Test
   fun checkWithdrawalFailedWithLowBalance() {
      accountService.saveAccount(CreateAccountRequest(BigDecimal(5)))
      assertFailsWith<ResponseStatusException>(
         message = "1 account's balance is too low"
      ) {
         transactionService.withdrawalMoney(createWithdrawalTransaction(1L))
      }
   }

}

fun createTransferTransaction(from: Long, to: Long): Transaction {
   return Transaction(
      txId = null,
      amount = BigDecimal(10),
      from = from,
      to = to,
      address = null,
      createdAt = Date(),
      status = TransactionStatus.PROCESSING,
      type = TransactionType.TRANSFER
   )
}

fun createWithdrawalTransaction(from: Long): Transaction {
   return Transaction(
      txId = null,
      amount = BigDecimal(10),
      from = from,
      to = null,
      address = "London",
      createdAt = Date(),
      status = TransactionStatus.PROCESSING,
      type = TransactionType.WITHDRAWAL
   )
}