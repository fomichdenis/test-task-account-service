package com.test.task.app.entity.requests

data class TransactionStatusRequest(
    var account: Long? = null,
    var txId: String? = null
)
