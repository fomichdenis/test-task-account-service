package com.test.task.app.entity.requests

import java.math.BigDecimal

data class CreateAccountRequest(
    var balance: BigDecimal? = null
)
