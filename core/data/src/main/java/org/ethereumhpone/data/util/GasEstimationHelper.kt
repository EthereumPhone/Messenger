package org.ethereumhpone.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.ethereumphone.walletsdk.WalletSDK
import java.math.BigInteger

/**
 * Helper class for estimating gas for UserOperations.
 * Uses exact values from API but always sets verificationGasLimit to 800k.
 * 
 * NOTE: WalletSDK internally doubles preVerificationGas and verificationGasLimit,
 * so we return half of the desired final values for these fields.
 */
object GasEstimationHelper {
    
    // EntryPoint v0.6 address
    private const val ENTRY_POINT = "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789"
    
    // Default fallback values if API fails
    private val DEFAULT_PRE_VERIFICATION_GAS = BigInteger.valueOf(70000) // 70k
    private val DEFAULT_CALL_GAS_LIMIT = BigInteger.valueOf(200000) // 200k
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Estimates gas for a UserOperation by calling eth_estimateUserOperationGas RPC.
     * Uses exact API values except for verificationGasLimit which is hardcoded to result in 800k after doubling.
     * 
     * Note: WalletSDK doubles preVerificationGas and verificationGasLimit internally,
     * so we return 400k for verificationGasLimit to achieve a final value of 800k.
     */
    suspend fun estimateGas(
        userOp: WalletSDK.UserOperation,
        rpcUrl: String
    ): WalletSDK.GasEstimation = withContext(Dispatchers.IO) {
        
        // Create UserOperation JSON object with proper field names and hex encoding
        val userOpJson = buildJsonObject {
            put("sender", JsonPrimitive(userOp.sender))
            put("nonce", JsonPrimitive("0x" + userOp.nonce.toString(16)))
            put("initCode", JsonPrimitive(userOp.initCode.ifEmpty { "0x" }))
            put("callData", JsonPrimitive(userOp.callData.ifEmpty { "0x" }))
            put("callGasLimit", JsonPrimitive("0x" + userOp.callGasLimit.toString(16)))
            put("verificationGasLimit", JsonPrimitive("0x" + userOp.verificationGasLimit.toString(16)))
            put("preVerificationGas", JsonPrimitive("0x" + userOp.preVerificationGas.toString(16)))
            put("maxFeePerGas", JsonPrimitive("0x" + userOp.maxFeePerGas.toString(16)))
            put("maxPriorityFeePerGas", JsonPrimitive("0x" + userOp.maxPriorityFeePerGas.toString(16)))
            put("paymasterAndData", JsonPrimitive(userOp.paymasterAndData.ifEmpty { "0x" }))
            put("signature", JsonPrimitive(userOp.signature))
        }
        
        // Create JSON-RPC request
        val requestJson = buildJsonObject {
            put("jsonrpc", JsonPrimitive("2.0"))
            put("method", JsonPrimitive("eth_estimateUserOperationGas"))
            put("params", buildJsonArray {
                add(userOpJson)
                add(JsonPrimitive(ENTRY_POINT))
            })
            put("id", JsonPrimitive(1))
        }
        
        // Initialize with defaults
        var preVerificationGas = DEFAULT_PRE_VERIFICATION_GAS
        var callGasLimit = DEFAULT_CALL_GAS_LIMIT
        
        try {
            // Make HTTP request to RPC endpoint
            val client = OkHttpClient()
            val contentType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestJson.toString().toRequestBody(contentType))
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        // Parse response
                        val responseJson = json.parseToJsonElement(responseBody).jsonObject
                        
                        if (responseJson.containsKey("error")) {
                            // Log error but use default values
                            val error = responseJson["error"]?.jsonObject
                            val errorMessage = error?.get("message")?.jsonPrimitive?.contentOrNull ?: "Unknown error"
                            println("GasEstimationHelper: API error: $errorMessage, using default values")
                        } else if (responseJson.containsKey("result")) {
                            val result = responseJson["result"]?.jsonObject
                            
                            // Extract exact values from API (no buffer applied)
                            result?.get("preVerificationGas")?.jsonPrimitive?.contentOrNull?.let { value ->
                                try {
                                    preVerificationGas = BigInteger(value.removePrefix("0x"), 16)
                                } catch (e: Exception) {
                                    println("GasEstimationHelper: Failed to parse preVerificationGas: ${e.message}")
                                }
                            }
                            
                            result?.get("callGasLimit")?.jsonPrimitive?.contentOrNull?.let { value ->
                                try {
                                    callGasLimit = BigInteger(value.removePrefix("0x"), 16)
                                } catch (e: Exception) {
                                    println("GasEstimationHelper: Failed to parse callGasLimit: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("GasEstimationHelper: Failed to parse response: ${e.message}")
                    }
                }
            } else {
                println("GasEstimationHelper: HTTP request failed with code ${response.code}, using defaults")
            }
        } catch (e: Exception) {
            println("GasEstimationHelper: Exception during gas estimation: ${e.message}, using defaults")
        }
        
        // WalletSDK internally doubles preVerificationGas and verificationGasLimit
        // So we need to return half of what we want the final values to be
        
        // We want final verificationGasLimit to be 800k, so return 400k
        val verificationGasLimitValue = BigInteger.valueOf(400000) // Will be doubled to 800k by WalletSDK
        
        WalletSDK.GasEstimation(
            preVerificationGas = preVerificationGas,
            verificationGasLimit = verificationGasLimitValue, // 400k -> will be doubled to 800k
            callGasLimit = callGasLimit
        )
    }
}
