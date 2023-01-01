package com.studiversity.transaction

import org.jetbrains.exposed.sql.transactions.transaction

interface TransactionWorker {
    operator fun <T> invoke(block: () -> T): T
}

class StubTransactionWorker : TransactionWorker {
    override fun <T> invoke(block: () -> T): T = block()
}

class DatabaseTransactionWorker : TransactionWorker {
    override fun <T> invoke(block: () -> T): T = transaction { block() }
}