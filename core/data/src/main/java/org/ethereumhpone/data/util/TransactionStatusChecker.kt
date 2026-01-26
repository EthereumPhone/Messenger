package org.ethereumhpone.data.util

import kotlinx.coroutines.delay
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.ClientConnectionException
import java.io.IOException

class TransactionStatusChecker {

    suspend fun getTxStatus(
        web3j: Web3j,
        txHash: String,
        pollIntervalMs: Long = 1_000L,
        timeoutMs: Long = 120_000L
    ): TxStatus {
        require(txHash.startsWith("0x") && txHash.length == 66) { "txHash must be a 32-byte hash (0x + 64 hex chars)" }
        require(pollIntervalMs >= 0) { "pollIntervalMs must be >= 0" }
        require(timeoutMs >= 0) { "timeoutMs must be >= 0" }

        val deadline = System.currentTimeMillis() + timeoutMs



        while (System.currentTimeMillis() < deadline) {
            checkStatus(web3j, txHash)?.let { return it }
            delay(pollIntervalMs)
        }

        // Final attempt after timeout
        return checkStatus(web3j, txHash) ?: TxStatus.UNKNOWN
    }

    private fun checkStatus(web3j: Web3j, txHash: String): TxStatus? {
        return try {
            val receiptOpt = web3j.ethGetTransactionReceipt(txHash).send().transactionReceipt
            if (receiptOpt.isPresent) {
                parseStatus(receiptOpt.get().status)
            } else {
                // No receipt yet - check if tx is at least known to the mempool
                val txOpt = web3j.ethGetTransactionByHash(txHash).send().transaction
                if (txOpt.isPresent) TxStatus.PENDING else TxStatus.UNKNOWN
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun parseStatus(statusHex: String?): TxStatus {
        return when (statusHex?.lowercase()) {
            "0x1", "0x01" -> TxStatus.SUCCESS
            "0x0", "0x00" -> TxStatus.FAILED
            else -> TxStatus.UNKNOWN // Either still pending
        }
    }
}

enum class TxStatus {
    PENDING,
    SUCCESS,
    FAILED,
    UNKNOWN
}