package org.ethereumhpone.data.repository

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.ethereumhpone.data.BuildConfig
import org.ethereumhpone.domain.repository.TokenRepository
import org.ethereumphone.model.TokenAsset
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor() : TokenRepository {

    private val client = OkHttpClient()

    override suspend fun getTokenAssets(address: String, chainId: Int): List<TokenAsset> {
        val apiKey = BuildConfig.ALCHEMY_API
        val networkName = chainIdToName(chainId)
        val url = "https://$networkName.g.alchemy.com/v2/$apiKey/getTokenBalances?address=$address&withMetadata=true"

        return try {
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("TokenRepository", "Unsuccessful response from Alchemy: ${response.code}")
                return emptyList()
            }
            val bodyString = response.body?.string() ?: return emptyList()
            parseTokenBalances(bodyString, chainId)
        } catch (e: Exception) {
            Log.e("TokenRepository", "Error fetching token balances", e)
            emptyList()
        }
    }

    private fun parseTokenBalances(rawJson: String, chainId: Int): List<TokenAsset> {
        val result = mutableListOf<TokenAsset>()
        val json = JSONObject(rawJson)
        val balancesArray = json.optJSONArray("tokenBalances") ?: return emptyList()

        for (i in 0 until balancesArray.length()) {
            val item = balancesArray.getJSONObject(i)

            // Skip entries with errors or zero balances
            if (item.opt("error") != null) continue

            val contractAddress = item.optString("contractAddress")
            val balanceHex = item.optString("tokenBalance", "0x0")
            val decimals = item.optInt("decimals", 18)
            val name = item.optString("name", "")
            val symbol = item.optString("symbol", "")

            val balanceBigInt = try {
                BigInteger(balanceHex.removePrefix("0x"), 16)
            } catch (e: Exception) {
                BigInteger.ZERO
            }

            if (balanceBigInt == BigInteger.ZERO) continue

            val balance = balanceBigInt.toBigDecimal()
                .divide(BigDecimal.TEN.pow(decimals))
                .toDouble()

            result.add(
                TokenAsset(
                    chainId = chainId,
                    name = name,
                    symbol = symbol,
                    balance = balance,
                    address = contractAddress,
                    decimals = decimals
                )
            )
        }
        return result
    }

    private fun chainIdToName(chainId: Int): String = when (chainId) {
        1 -> "eth-mainnet"
        5 -> "eth-goerli"
        11155111 -> "eth-sepolia"
        10 -> "opt-mainnet"
        42161 -> "arb-mainnet"
        137 -> "polygon-mainnet"
        8453 -> "base-mainnet"
        else -> "eth-mainnet"
    }
} 