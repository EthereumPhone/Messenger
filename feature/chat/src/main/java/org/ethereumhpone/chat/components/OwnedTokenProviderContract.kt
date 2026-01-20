package org.ethereumhpone.chat.components

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import java.math.BigDecimal

/**
 * Contract class for querying owned tokens from WalletManager's ContentProviders.
 * 
 * Uses the same approach as TokenLauncher:
 * 1. Query TokenBalanceContentProvider for tokens with positive balance
 * 2. Query TokenMetadataContentProvider for metadata (name, symbol, logo, price)
 * 3. Combine the results
 * 
 * Authorities (same as TokenLauncher):
 * - com.walletmanager.tokenbalance.provider
 * - com.walletmanager.tokenmetadata.provider
 */
object OwnedTokenProviderContract {
    
    private const val TAG = "OwnedTokenProviderContract"
    
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
    
    /**
     * Data class representing an owned token with balance
     */
    data class OwnedTokenData(
        val contractAddress: String,
        val decimals: Int,
        val name: String,
        val symbol: String,
        val logo: String?,
        val chainId: Int,
        val swappable: Boolean,
        val balance: BigDecimal,
        val price: Double,
        val chains: List<Int>
    ) {
        /**
         * Get the fiat value of this token holding
         */
        val fiatValue: Double
            get() = balance.toDouble() * price
        
        /**
         * Convert to TokenOption for transaction requests
         */
        fun toTokenOption(): TokenOption {
            return TokenOption(
                symbol = symbol,
                name = name,
                decimals = decimals,
                contractAddress = if (isNativeToken()) null else contractAddress,
                chainId = chainId.toLong(),
                logoUrl = logo,
                balance = balance,
                price = price
            )
        }
        
        /**
         * Check if this is a native token (ETH, MATIC, etc.)
         */
        fun isNativeToken(): Boolean {
            return contractAddress.lowercase() == "0x0000000000000000000000000000000000000000" ||
                   contractAddress == chainId.toString()
        }
    }
    
    /**
     * Query all owned tokens from WalletManager (tokens with positive balance).
     * Uses the same approach as TokenLauncher:
     * 1. Get positive balances from TokenBalanceContentProvider
     * 2. Get metadata from TokenMetadataContentProvider for each
     */
    fun getAllOwnedTokens(contentResolver: ContentResolver): List<OwnedTokenData> {
        Log.d(TAG, "=== Fetching all owned tokens ===")
        
        // Step 1: Get all positive balances
        println("ETHOSDEBUG Get all positive balances")
        val balances = getBalancesWithPositiveBalance(contentResolver)
        Log.d(TAG, "Found ${balances.size} tokens with positive balance")
        
        if (balances.isEmpty()) {
            return emptyList()
        }
        
        // Step 2: Get metadata for each token and combine
        val ownedTokens = mutableListOf<OwnedTokenData>()
        for ((contractAddress, chainId, tokenBalance) in balances) {
            val metadata = getTokenMetadata(contentResolver, chainId, contractAddress)
            if (metadata != null) {
                ownedTokens.add(
                    OwnedTokenData(
                        contractAddress = contractAddress,
                        decimals = metadata.decimals,
                        name = metadata.name,
                        symbol = metadata.symbol,
                        logo = metadata.logo,
                        chainId = chainId,
                        swappable = metadata.swappable,
                        balance = tokenBalance,
                        price = metadata.price,
                        chains = listOf(chainId)
                    )
                )
                Log.d(TAG, "Added token: ${metadata.symbol} balance=$tokenBalance price=${metadata.price}")
            } else {
                Log.w(TAG, "No metadata found for $contractAddress on chain $chainId")
            }
        }
        
        Log.d(TAG, "Returning ${ownedTokens.size} owned tokens")
        return ownedTokens
    }
    
    /**
     * Query owned tokens for a specific chain from WalletManager
     */
    fun getOwnedTokensByChain(contentResolver: ContentResolver, chainId: Int): List<OwnedTokenData> {
        Log.d(TAG, "Fetching owned tokens for chain $chainId")
        
        val balances = getBalancesByChain(contentResolver, chainId)
        Log.d(TAG, "Found ${balances.size} tokens on chain $chainId")
        
        if (balances.isEmpty()) {
            return emptyList()
        }
        
        val ownedTokens = mutableListOf<OwnedTokenData>()
        for ((contractAddress, cId, tokenBalance) in balances) {
            val metadata = getTokenMetadata(contentResolver, cId, contractAddress)
            if (metadata != null) {
                ownedTokens.add(
                    OwnedTokenData(
                        contractAddress = contractAddress,
                        decimals = metadata.decimals,
                        name = metadata.name,
                        symbol = metadata.symbol,
                        logo = metadata.logo,
                        chainId = cId,
                        swappable = metadata.swappable,
                        balance = tokenBalance,
                        price = metadata.price,
                        chains = listOf(cId)
                    )
                )
            }
        }
        
        return ownedTokens
    }
    
    /**
     * Query a specific owned token from WalletManager
     */
    fun getOwnedToken(contentResolver: ContentResolver, chainId: Int, contractAddress: String): OwnedTokenData? {
        val balance = getTokenBalance(contentResolver, chainId, contractAddress) ?: return null
        val metadata = getTokenMetadata(contentResolver, chainId, contractAddress) ?: return null
        
        return OwnedTokenData(
            contractAddress = contractAddress,
            decimals = metadata.decimals,
            name = metadata.name,
            symbol = metadata.symbol,
            logo = metadata.logo,
            chainId = chainId,
            swappable = metadata.swappable,
            balance = balance,
            price = metadata.price,
            chains = listOf(chainId)
        )
    }
    
    // =====================================================
    // TokenBalance Provider Methods (same as TokenLauncher)
    // =====================================================
    
    private data class BalanceData(
        val contractAddress: String,
        val chainId: Int,
        val tokenBalance: BigDecimal
    )
    
    private fun getBalancesWithPositiveBalance(contentResolver: ContentResolver): List<BalanceData> {
        val uri = BALANCE_URI.buildUpon()
            .appendPath("balances")
            .appendPath("positive")
            .build()
        
        Log.d(TAG, "=== Querying positive balances ===")
        Log.d(TAG, "URI: $uri")
        Log.d(TAG, "Authority: $BALANCE_AUTHORITY")
        
        return try {
            Log.d(TAG, "Calling contentResolver.query()...")
            val cursor = contentResolver.query(uri, null, null, null, null)
            Log.d(TAG, "Cursor returned: ${cursor != null}")
            
            cursor?.use {
                val balances = mutableListOf<BalanceData>()
                val count = it.count
                Log.d(TAG, "Balance cursor has $count rows")
                
                if (count == 0) {
                    Log.w(TAG, "⚠️ No rows returned from balance provider - WalletManager may not have any positive balances")
                }
                
                // Log available columns
                val columnNames = it.columnNames?.joinToString(", ") ?: "null"
                Log.d(TAG, "Available columns: $columnNames")
                
                while (it.moveToNext()) {
                    try {
                        val address = it.getString(it.getColumnIndexOrThrow(COL_CONTRACT_ADDRESS))
                        val chainId = it.getInt(it.getColumnIndexOrThrow(COL_CHAIN_ID))
                        val balanceStr = it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE))
                        Log.d(TAG, "Row: address=$address chainId=$chainId balance=$balanceStr")
                        
                        balances.add(
                            BalanceData(
                                contractAddress = address,
                                chainId = chainId,
                                tokenBalance = balanceStr.toBigDecimal()
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing balance row", e)
                    }
                }
                Log.d(TAG, "Parsed ${balances.size} balance entries")
                balances
            } ?: run {
                Log.w(TAG, "⚠️ Balance cursor is null - ContentProvider may not be available or accessible")
                // Additional diagnostics when cursor is null
                Log.w(TAG, "⚠️ Possible causes:")
                Log.w(TAG, "   1. WalletManager app not installed")
                Log.w(TAG, "   2. Permission 'com.walletmanager.permission.READ_TOKEN_METADATA' not granted")
                Log.w(TAG, "   3. ContentProvider initialization failed in WalletManager")
                Log.w(TAG, "   Try: adb shell content query --uri $uri")
                emptyList()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException - Permission denied! Make sure Messenger declares: <uses-permission android:name=\"com.walletmanager.permission.READ_TOKEN_METADATA\" />", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error querying positive balances: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun getBalancesByChain(contentResolver: ContentResolver, chainId: Int): List<BalanceData> {
        val uri = BALANCE_URI.buildUpon()
            .appendPath("balances")
            .appendPath(chainId.toString())
            .build()
        
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val balances = mutableListOf<BalanceData>()
                while (it.moveToNext()) {
                    try {
                        balances.add(
                            BalanceData(
                                contractAddress = it.getString(it.getColumnIndexOrThrow(COL_CONTRACT_ADDRESS)),
                                chainId = it.getInt(it.getColumnIndexOrThrow(COL_CHAIN_ID)),
                                tokenBalance = it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE)).toBigDecimal()
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing balance row", e)
                    }
                }
                balances
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error querying balances for chain $chainId", e)
            emptyList()
        }
    }
    
    private fun getTokenBalance(contentResolver: ContentResolver, chainId: Int, contractAddress: String): BigDecimal? {
        val uri = BALANCE_URI.buildUpon()
            .appendPath("balance")
            .appendPath(chainId.toString())
            .appendPath(contractAddress)
            .build()
        
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(COL_TOKEN_BALANCE)).toBigDecimal()
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying balance for $contractAddress", e)
            null
        }
    }
    
    // =====================================================
    // TokenMetadata Provider Methods (same as TokenLauncher)
    // =====================================================
    
    private data class MetadataData(
        val contractAddress: String,
        val decimals: Int,
        val name: String,
        val symbol: String,
        val logo: String?,
        val chainId: Int,
        val swappable: Boolean,
        val price: Double
    )
    
    private fun getTokenMetadata(contentResolver: ContentResolver, chainId: Int, contractAddress: String): MetadataData? {
        val uri = METADATA_URI.buildUpon()
            .appendPath("token")
            .appendPath(chainId.toString())
            .appendPath(contractAddress)
            .build()
        
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    MetadataData(
                        contractAddress = it.getString(it.getColumnIndexOrThrow(COL_CONTRACT_ADDRESS)),
                        decimals = it.getInt(it.getColumnIndexOrThrow(COL_DECIMALS)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_NAME)),
                        symbol = it.getString(it.getColumnIndexOrThrow(COL_SYMBOL)),
                        logo = it.getString(it.getColumnIndexOrThrow(COL_LOGO)),
                        chainId = it.getInt(it.getColumnIndexOrThrow(COL_CHAIN_ID)),
                        swappable = it.getInt(it.getColumnIndexOrThrow(COL_SWAPPABLE)) == 1,
                        price = it.getDouble(it.getColumnIndexOrThrow(COL_PRICE))
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying metadata for $contractAddress on chain $chainId", e)
            null
        }
    }
}
