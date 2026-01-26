package org.ethereumhpone.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.ClientConnectionException
import java.io.IOException

class TransactionStatusChecker {

    private val httpClient = OkHttpClient()

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

    suspend fun getDetailedTxStatus(
        web3j: Web3j,
        rpcUrl: String,
        txHash: String,
        checkInternalReverts: Boolean = true,
        pollIntervalMs: Long = 1_000L,
        timeoutMs: Long = 120_000L
    ): TxStatus {
        require(txHash.startsWith("0x") && txHash.length == 66) { "txHash must be a 32-byte hash (0x + 64 hex chars)" }
        require(pollIntervalMs >= 0) { "pollIntervalMs must be >= 0" }
        require(timeoutMs >= 0) { "timeoutMs must be >= 0" }

        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            val status = checkDetailedStatus(web3j, rpcUrl, txHash, checkInternalReverts)
            if (status != null) return status
            delay(pollIntervalMs)
        }

        // Final attempt after timeout
        return checkDetailedStatus(web3j, rpcUrl, txHash, checkInternalReverts) ?: TxStatus.UNKNOWN
    }

    private suspend fun checkDetailedStatus(
        web3j: Web3j,
        rpcUrl: String,
        txHash: String,
        checkInternalReverts: Boolean
    ): TxStatus? {
        return try {
            val receiptOpt = web3j.ethGetTransactionReceipt(txHash).send().transactionReceipt
            if (receiptOpt.isPresent) {
                val receipt = receiptOpt.get()
                val basicStatus = parseStatus(receipt.status)
                
                // If the transaction succeeded but we want to check for internal reverts
                if (basicStatus == TxStatus.SUCCESS && checkInternalReverts) {
                    if (hasInternalRevert(rpcUrl, txHash)) {
                        return TxStatus.REVERTED
                    }
                }
                
                basicStatus
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

    /**
     * Check if a transaction has any internal reverts using trace_transaction API.
     * This is useful for detecting reverts in internal calls that were caught by try/catch.
     *
     * @param rpcUrl The RPC URL (must support trace_transaction)
     * @param txHash The transaction hash to check
     * @return true if any internal call reverted, false otherwise
     */
    private suspend fun hasInternalRevert(rpcUrl: String, txHash: String): Boolean {
        require(txHash.startsWith("0x") && txHash.length == 66) { "txHash must be a 32-byte hash (0x + 64 hex chars)" }

        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "trace_transaction")
                    put("params", JSONArray().put(txHash))
                    put("id", 1)
                }

                val request = Request.Builder()
                    .url(rpcUrl)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext false

                val jsonResponse = JSONObject(responseBody)
                val result = jsonResponse.optJSONArray("result") ?: return@withContext false

                // Check each trace for errors/reverts
                for (i in 0 until result.length()) {
                    val trace = result.getJSONObject(i)
                    val error = trace.optString("error", "")
                    if (error.isNotEmpty()) {
                        // Found an internal revert
                        return@withContext true
                    }
                }

                false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
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
            else -> TxStatus.UNKNOWN
        }
    }
}

enum class TxStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REVERTED,
    UNKNOWN,
}