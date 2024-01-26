package com.test.task.app.entity.requests

import java.math.BigDecimal

data class WithdrawalRequest(
    var account: Long? = null,
    var address: String? = null,
    var amount: BigDecimal? = null
)
