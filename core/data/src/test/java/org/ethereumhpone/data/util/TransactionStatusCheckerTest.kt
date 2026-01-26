package org.ethereumhpone.data.util

import io.basenameservice.examples.RPC_URL
import kotlinx.coroutines.test.runTest
import org.ethereumhpone.data.BuildConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

/**
 * Unit tests for [TransactionStatusChecker].
 * 
 * These tests use a real RPC endpoint (Alchemy) to verify transaction status checking.
 * Make sure you have a valid ALCHEMY_API key in local.properties.
 */
class TransactionStatusCheckerTest {

    private lateinit var web3j: Web3j
    private lateinit var web3JOptimism: Web3j
    private lateinit var checker: TransactionStatusChecker

    // RPC URL using Alchemy - uses the API key from local.properties
    // For unit tests, we hardcode it here since BuildConfig is not available in unit tests
    private val rpcMainnetUrl = "https://eth-mainnet.g.alchemy.com/v2/${BuildConfig.ALCHEMY_API}"
    private val rpcOptimismUrl = "https://opt-mainnet.g.alchemy.com/v2/${BuildConfig.ALCHEMY_API}"

    // Known transaction hashes for testing (mainnet transactions)
    // This is a successful transaction
    private val knownSuccessfulTxHash = "0x580e98fa968a5e83be34a9f9bdf4138067f2652e76cb1258cf0d9639ac5e3874"
    
    // A known reverted transaction (on mainnet)
    // You can find failed transactions on etherscan by looking for ones with "Fail" status
    // Example: https://etherscan.io/tx/0x... with Status: Fail
    private val knownFailedTxHash = "0x67ec3acc5274a88c50d1e79e9b9d4c2c3d5e0e3ba3cc33b32d65f3fdb3b5a258"

    private val knwonRevertedTxHash = "0x1b93847129b5f0427d531408b83a2fdaa0e9d3a5e5d171e6c3b0e593819cc0e2"
    
    // A non-existent transaction hash (valid format but doesn't exist)
    private val nonExistentTxHash = "0x0000000000000000000000000000000000000000000000000000000000000001"

    @Before
    fun setUp() {
        web3j = Web3j.build(HttpService(rpcMainnetUrl))
        web3JOptimism = Web3j.build(HttpService(rpcOptimismUrl))
        checker = TransactionStatusChecker()
    }

    @Test
    fun `getTxStatus returns SUCCESS for a known successful transaction`() = runTest {
        val status = checker.getTxStatus(
            web3j = web3j,
            txHash = knownSuccessfulTxHash,
            pollIntervalMs = 100L,
            timeoutMs = 10_000L
        )

        assertEquals(TxStatus.SUCCESS, status)
    }

    @Test
    fun `getTxStatus returns UNKNOWN for non-existent transaction`() = runTest {
        val status = checker.getTxStatus(
            web3j = web3j,
            txHash = nonExistentTxHash,
            pollIntervalMs = 100L,
            timeoutMs = 1_000L  // Short timeout since we know it won't be found
        )

        assertEquals(TxStatus.UNKNOWN, status)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getTxStatus throws IllegalArgumentException for invalid hash - missing 0x prefix`() = runTest {
        checker.getTxStatus(
            web3j = web3j,
            txHash = "5c504ed432cb51138bcf09aa5e8a410dd4a1e204ef84bfed1be16dfba1b22060",
            pollIntervalMs = 100L,
            timeoutMs = 1_000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getTxStatus throws IllegalArgumentException for invalid hash - wrong length`() = runTest {
        checker.getTxStatus(
            web3j = web3j,
            txHash = "0x5c504ed432cb51138bcf09aa5e8a410dd4a1e204ef84bfed1be16dfba1b2206", // 65 chars instead of 66
            pollIntervalMs = 100L,
            timeoutMs = 1_000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getTxStatus throws IllegalArgumentException for negative pollIntervalMs`() = runTest {
        checker.getTxStatus(
            web3j = web3j,
            txHash = knownSuccessfulTxHash,
            pollIntervalMs = -1L,
            timeoutMs = 1_000L
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getTxStatus throws IllegalArgumentException for negative timeoutMs`() = runTest {
        checker.getTxStatus(
            web3j = web3j,
            txHash = knownSuccessfulTxHash,
            pollIntervalMs = 100L,
            timeoutMs = -1L
        )
    }

    @Test
    fun `getTxStatus with zero timeout returns status immediately`() = runTest {
        val status = checker.getTxStatus(
            web3j = web3j,
            txHash = knownSuccessfulTxHash,
            pollIntervalMs = 100L,
            timeoutMs = 0L
        )

        // With zero timeout, it should still make one attempt and return the status
        assertEquals(TxStatus.SUCCESS, status)
    }

    @Test
    fun `getTxStatus returns FAILED for a known reverted transaction on mainnet`() = runTest {
        val status = checker.getTxStatus(
            web3j = web3j,
            txHash = knownFailedTxHash,
            pollIntervalMs = 100L,
            timeoutMs = 10_000L
        )

        assertEquals(TxStatus.FAILED, status)
    }

    @Test
    fun `web3j connection is working`() {
        // Simple sanity check that the RPC connection works
        val clientVersion = web3j.web3ClientVersion().send()
        assertNotNull(clientVersion.web3ClientVersion)
        println("Connected to: ${clientVersion.web3ClientVersion}")
    }

    @Test
    fun `can retrieve block number`() {
        // Another sanity check for RPC connectivity
        val blockNumber = web3j.ethBlockNumber().send()
        assertNotNull(blockNumber.blockNumber)
        assertTrue(blockNumber.blockNumber.toLong() > 0)
        println("Current block number: ${blockNumber.blockNumber}")
    }

    @Test
    fun `debug - print receipt status for failed tx`() {
        // Debug test to see what the actual receipt status is
        val receiptResponse = web3j.ethGetTransactionReceipt(knownFailedTxHash).send()
        val receipt = receiptResponse.transactionReceipt.orElse(null)
        
        assertNotNull("Receipt should exist", receipt)
        println("Transaction hash: ${receipt.transactionHash}")
        println("Status field: ${receipt.status}")
        println("Is status success (0x1)?: ${receipt.isStatusOK}")
        println("Block number: ${receipt.blockNumber}")
        println("Gas used: ${receipt.gasUsed}")
    }

    // ==================== Internal Revert Detection Tests ====================


    @Test
    fun `getDetailedTxStatus returns REVERTED for transaction with internal revert`() = runTest {
        val status = checker.getDetailedTxStatus(
            web3j = web3j,
            rpcUrl = rpcMainnetUrl,
            txHash = knwonRevertedTxHash,
            checkInternalReverts = true,
            pollIntervalMs = 100L,
            timeoutMs = 10_000L
        )

        assertEquals(TxStatus.REVERTED, status)
    }

    @Test
    fun `getDetailedTxStatus returns SUCCESS for clean transaction`() = runTest {
        val status = checker.getDetailedTxStatus(
            web3j = web3j,
            rpcUrl = rpcMainnetUrl,
            txHash = knownSuccessfulTxHash,
            checkInternalReverts = true,
            pollIntervalMs = 100L,
            timeoutMs = 10_000L
        )

        assertEquals(TxStatus.SUCCESS, status)
    }

    @Test
    fun `getDetailedTxStatus without internal revert check returns SUCCESS even for reverted tx`() = runTest {
        // When checkInternalReverts is false, we only check the receipt status
        val status = checker.getDetailedTxStatus(
            web3j = web3j,
            rpcUrl = rpcMainnetUrl,
            txHash = knwonRevertedTxHash,
            checkInternalReverts = false,  // Disable internal revert checking
            pollIntervalMs = 100L,
            timeoutMs = 10_000L
        )

        // Should return SUCCESS since the receipt status is 0x1
        assertEquals(TxStatus.SUCCESS, status)
    }
}
