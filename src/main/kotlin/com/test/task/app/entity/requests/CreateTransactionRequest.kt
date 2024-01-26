package com.test.task.app.entity.requests

import java.math.BigDecimal

data class CreateTransactionRequest(
    var amount: BigDecimal? = null,
    var from: Long? = null,
    var to: Long? = null
)