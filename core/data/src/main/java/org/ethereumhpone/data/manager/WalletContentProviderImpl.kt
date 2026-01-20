package org.ethereumhpone.data.manager

import android.content.Context
import android.net.Uri
import android.database.Cursor
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ethereumhpone.domain.manager.WalletContentProvider
import org.ethereumhpone.domain.model.OwnedToken
import org.ethereumhpone.domain.model.TokenMetadata
import javax.inject.Inject

/**
 * Implementation of WalletContentProvider that queries WalletManager's ContentProviders.
 * 
 * Uses the same approach as TokenLauncher:
 * - TokenBalanceContentProvider (com.walletmanager.tokenbalance.provider)
 * - TokenMetadataContentProvider (com.walletmanager.tokenmetadata.provider)
 */
class WalletContentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WalletContentProvider {

    companion object {
        private const val TAG = "WalletContentProviderImpl"
        
        // TokenBalance provider (same as TokenLauncher)
        private const val BALANCE_AUTHORITY = "com.walletmanager.tokenbalance.provider"
        private val BALANCE_URI: Uri = Uri.parse("content://$BALANCE_AUTHORITY")
        
        // TokenMetadata provider (same as TokenLauncher)
        private const val METADATA_AUTHORITY = "com.walletmanager.tokenmetadata.provider"
        private val METADATA_URI: Uri = Uri.parse("content://$METADATA_AUTHORITY")
        
        // Column names for TokenBalance provider
        private const val COL_CONTRACT_ADDRESS = "contract_address"
        private const val COL_CHAIN_ID = "chain_id"
        private const val COL_TOKEN_BALANCE = "token_balance"
        
        // Column names for TokenMetadata provider
        private const val COL_DECIMALS = "decimals"
        private const val COL_NAME = "name"
        private const val COL_SYMBOL = "symbol"
        private const val COL_LOGO = "logo"
        private const val COL_SWAPPABLE = "swappable"
        private const val COL_PRICE = "price"
    }

    override fun getTokenMetadata(
        chainId: String,
        contractAddress: String
    ): TokenMetadata? {
        val uri = METADATA_URI.buildUpon()
            .appendPath("token")
            .appendPath(chainId)
            .appendPath(contractAddress)
            .build()
        
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    parseTokenMetadataCursor(it)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying token metadata for $contractAddress on chain $chainId", e)
            null
        }
    }

    override fun getTokensMetadataForChain(chainId: String): List<TokenMetadata> {
        val uri = METADATA_URI.buildUpon()
            .appendPath("tokens")
            .appendPath(chainId)
            .build()
        
        return try {
            val list = mutableListOf<TokenMetadata>()
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                while (it.moveToNext()) {
                    parseTokenMetadataCursor(it)?.let(list::add)
                }
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error querying tokens metadata for chain $chainId", e)
            emptyList()
        }
    }

    override fun getOwnedToken(
        chainId: String,
        contractAddress: String
    ): OwnedToken? {
        // Get balance
        val balanceUri = BALANCE_URI.buildUpon()
            .appendPath("balance")
            .appendPath(chainId)
            .appendPath(contractAddress)
            .build()
        
        val balance = try {
            val cursor = context.contentResolver.query(balanceUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE))
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying balance", e)
            null
        } ?: return null
        
        // Get metadata
        val metadata = getTokenMetadata(chainId, contractAddress) ?: return null
        
        // Get price
        val price = getTokenPrice(chainId, contractAddress)
        
        return OwnedToken(
            contractAddress = metadata.contractAddress,
            decimals = metadata.decimals,
            name = metadata.name,
            symbol = metadata.symbol,
            logo = metadata.logo,
            chainId = metadata.chainId,
            swappable = metadata.swappable,
            balance = balance,
            price = price
        )
    }

    override fun getOwnedTokensForChain(chainId: String): List<OwnedToken> {
        val balanceUri = BALANCE_URI.buildUpon()
            .appendPath("balances")
            .appendPath(chainId)
            .build()
        
        Log.d(TAG, "Fetching owned tokens for chain $chainId")
        
        return try {
            val balances = mutableListOf<Pair<String, String>>() // contractAddress to balance
            val cursor = context.contentResolver.query(balanceUri, null, null, null, null)
            cursor?.use {
                while (it.moveToNext()) {
                    try {
                        val addr = it.getString(it.getColumnIndexOrThrow(COL_CONTRACT_ADDRESS))
                        val bal = it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE))
                        balances.add(addr to bal)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing balance row", e)
                    }
                }
            }
            
            Log.d(TAG, "Found ${balances.size} balances for chain $chainId")
            
            balances.mapNotNull { (contractAddress, balance) ->
                val metadata = getTokenMetadata(chainId, contractAddress) ?: return@mapNotNull null
                val price = getTokenPrice(chainId, contractAddress)
                OwnedToken(
                    contractAddress = metadata.contractAddress,
                    decimals = metadata.decimals,
                    name = metadata.name,
                    symbol = metadata.symbol,
                    logo = metadata.logo,
                    chainId = metadata.chainId,
                    swappable = metadata.swappable,
                    balance = balance,
                    price = price
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting owned tokens for chain $chainId", e)
            emptyList()
        }
    }

    override fun getAllOwnedTokens(): List<OwnedToken> {
        val balanceUri = BALANCE_URI.buildUpon()
            .appendPath("balances")
            .appendPath("positive")
            .build()
        
        Log.d(TAG, "=== Fetching all owned tokens from $balanceUri ===")
        
        return try {
            data class BalanceInfo(val contractAddress: String, val chainId: Int, val balance: String)
            val balances = mutableListOf<BalanceInfo>()
            
            val cursor = context.contentResolver.query(balanceUri, null, null, null, null)
            cursor?.use {
                Log.d(TAG, "Balance cursor has ${it.count} rows")
                while (it.moveToNext()) {
                    try {
                        balances.add(
                            BalanceInfo(
                                contractAddress = it.getString(it.getColumnIndexOrThrow(COL_CONTRACT_ADDRESS)),
                                chainId = it.getInt(it.getColumnIndexOrThrow(COL_CHAIN_ID)),
                                balance = it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE))
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing balance row", e)
                    }
                }
            } ?: run {
                Log.w(TAG, "Balance cursor is null")
            }
            
            Log.d(TAG, "Found ${balances.size} positive balances")
            
            val tokens = balances.mapNotNull { (contractAddress, chainId, balance) ->
                val metadata = getTokenMetadata(chainId.toString(), contractAddress)
                if (metadata == null) {
                    Log.w(TAG, "No metadata for $contractAddress on chain $chainId")
                    return@mapNotNull null
                }
                val price = getTokenPrice(chainId.toString(), contractAddress)
                
                OwnedToken(
                    contractAddress = metadata.contractAddress,
                    decimals = metadata.decimals,
                    name = metadata.name,
                    symbol = metadata.symbol,
                    logo = metadata.logo,
                    chainId = metadata.chainId,
                    swappable = metadata.swappable,
                    balance = balance,
                    price = price
                ).also {
                    Log.d(TAG, "Created token: ${it.symbol} balance=${it.balance} price=${it.price}")
                }
            }
            
            Log.d(TAG, "Returning ${tokens.size} owned tokens")
            tokens
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all owned tokens", e)
            emptyList()
        }
    }
    
    private fun getTokenPrice(chainId: String, contractAddress: String): Double {
        val uri = METADATA_URI.buildUpon()
            .appendPath("token")
            .appendPath(chainId)
            .appendPath(contractAddress)
            .build()
        
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val priceIdx = it.getColumnIndex(COL_PRICE)
                    if (priceIdx != -1) it.getDouble(priceIdx) else 0.0
                } else 0.0
            } ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun parseTokenMetadataCursor(cursor: Cursor): TokenMetadata? {
        val contractIdx = cursor.getColumnIndex(COL_CONTRACT_ADDRESS)
        if (contractIdx == -1) return null
        
        val decimalsIdx = cursor.getColumnIndex(COL_DECIMALS)
        val nameIdx = cursor.getColumnIndex(COL_NAME)
        val symbolIdx = cursor.getColumnIndex(COL_SYMBOL)
        val logoIdx = cursor.getColumnIndex(COL_LOGO)
        val chainIdx = cursor.getColumnIndex(COL_CHAIN_ID)
        val swapIdx = cursor.getColumnIndex(COL_SWAPPABLE)

        return try {
            TokenMetadata(
                contractAddress = cursor.getString(contractIdx),
                decimals = if (decimalsIdx != -1) cursor.getInt(decimalsIdx) else 18,
                name = if (nameIdx != -1) cursor.getString(nameIdx) ?: "" else "",
                symbol = if (symbolIdx != -1) cursor.getString(symbolIdx) ?: "" else "",
                logo = if (logoIdx != -1) cursor.getString(logoIdx) else null,
                chainId = if (chainIdx != -1) cursor.getInt(chainIdx) else 1,
                swappable = if (swapIdx != -1) cursor.getInt(swapIdx) == 1 else false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing token metadata cursor", e)
            null
        }
    }
} 