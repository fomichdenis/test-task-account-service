package com.test.task.app.entity.response

import com.test.task.app.entity.TransactionStatus
import com.test.task.app.entity.TransactionType
import java.math.BigDecimal
import java.util.*

data class TransactionResponse(
   var transactionId: String?,
   var amount: BigDecimal,
   var from: Long,
   var to: Long?,
   var address: String?,
   var type: TransactionType,
   var status: TransactionStatus,
   var createdAt: Date
)