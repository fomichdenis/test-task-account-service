package com.test.task.app.service

interface TransactionStatusUpdateService {
   fun updateStatus(txId: String, account: Long)
}
