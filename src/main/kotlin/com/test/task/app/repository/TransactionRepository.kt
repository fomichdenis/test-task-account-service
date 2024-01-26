package com.test.task.app.repository

import com.test.task.app.entity.Transaction
import org.springframework.data.repository.CrudRepository

interface TransactionRepository: CrudRepository<Transaction, String> {
}