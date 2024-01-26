package com.test.task.app.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

const val DELAY_TRANSACTION_UPDATE_MILLISECONDS = 11000L

@Component
class TransactionStatusUpdateServiceImpl(
   private val transactionService: TransactionService
): TransactionStatusUpdateService {

   override fun updateStatus(txId: String, account: Long) {
      val timer = Timer()
      val task = object : TimerTask() {
         override fun run() {
               transactionService.updateTransactionStatusWithRefundIfNeeded(txId, account)
               timer.cancel()
         }
      }
      timer.schedule(task, DELAY_TRANSACTION_UPDATE_MILLISECONDS)
   }
}