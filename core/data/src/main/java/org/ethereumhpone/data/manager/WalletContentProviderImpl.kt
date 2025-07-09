package org.ethereumhpone.data.manager

import android.content.Context
import android.net.Uri
import android.database.Cursor
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ethereumhpone.domain.manager.WalletContentProvider
import org.ethereumhpone.domain.model.OwnedToken
import org.ethereumhpone.domain.model.TokenMetadata
import javax.inject.Inject

class WalletContentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WalletContentProvider {

    private fun buildBaseUri(authoritySuffix: String): Uri.Builder {
        val authority = "$WALLET_PKG.$authoritySuffix"
        return Uri.parse("content://$authority").buildUpon()
    }

    override fun getTokenMetadata(
        chainId: String,
        contractAddress: String
    ): TokenMetadata? {
        val uri = buildBaseUri(TOKEN_METADATA_AUTHORITY)
            .appendPath("token")
            .appendPath(chainId)
            .appendPath(contractAddress)
            .build()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return parseTokenMetadataCursor(it)
            }
        }
        return null
    }

    override fun getTokensMetadataForChain(chainId: String): List<TokenMetadata> {
        val uri = buildBaseUri(TOKEN_METADATA_AUTHORITY)
            .appendPath("tokens")
            .appendPath(chainId)
            .build()
        val list = mutableListOf<TokenMetadata>()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                parseTokenMetadataCursor(it)?.let(list::add)
            }
        }
        return list
    }

    override fun getOwnedToken(
        chainId: String,
        contractAddress: String
    ): OwnedToken? {
        val uri = buildBaseUri(OWNED_TOKEN_AUTHORITY)
            .appendPath("ownedToken")
            .appendPath(chainId)
            .appendPath(contractAddress)
            .build()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return parseOwnedTokenCursor(it)
            }
        }
        return null
    }

    override fun getOwnedTokensForChain(chainId: String): List<OwnedToken> {
        val uri = buildBaseUri(OWNED_TOKEN_AUTHORITY)
            .appendPath("ownedTokens")
            .appendPath(chainId)
            .build()
        return queryOwnedTokens(uri)
    }

    override fun getAllOwnedTokens(): List<OwnedToken> {
        val uri = buildBaseUri(OWNED_TOKEN_AUTHORITY)
            .appendPath("ownedTokens")
            .build()
        return queryOwnedTokens(uri)
    }

    private fun queryOwnedTokens(uri: Uri): List<OwnedToken> {
        val list = mutableListOf<OwnedToken>()
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                parseOwnedTokenCursor(it)?.let(list::add)
            }
        }
        return list
    }

    private fun parseTokenMetadataCursor(cursor: Cursor): TokenMetadata? {
        val contractIdx = cursor.getColumnIndex("contract_address")
        if (contractIdx == -1) return null
        val decimalsIdx = cursor.getColumnIndex("decimals")
        val nameIdx = cursor.getColumnIndex("name")
        val symbolIdx = cursor.getColumnIndex("symbol")
        val logoIdx = cursor.getColumnIndex("logo")
        val chainIdx = cursor.getColumnIndex("chain_id")
        val swapIdx = cursor.getColumnIndex("swappable")

        val contractAddress = cursor.getString(contractIdx)
        val decimals = cursor.getInt(decimalsIdx)
        val name = cursor.getString(nameIdx)
        val symbol = cursor.getString(symbolIdx)
        val logo = cursor.getString(logoIdx)
        val chainId = cursor.getInt(chainIdx)
        val swappable = cursor.getInt(swapIdx) == 1

        return TokenMetadata(
            contractAddress = contractAddress,
            decimals = decimals,
            name = name,
            symbol = symbol,
            logo = logo,
            chainId = chainId,
            swappable = swappable
        )
    }

    private fun parseOwnedTokenCursor(cursor: Cursor): OwnedToken? {
        val meta = parseTokenMetadataCursor(cursor) ?: return null
        val balanceIdx = cursor.getColumnIndex("balance")
        val balance = if (balanceIdx != -1) cursor.getString(balanceIdx) else "0"
        return OwnedToken(
            contractAddress = meta.contractAddress,
            decimals = meta.decimals,
            name = meta.name,
            symbol = meta.symbol,
            logo = meta.logo,
            chainId = meta.chainId,
            swappable = meta.swappable,
            balance = balance
        )
    }

    companion object {
        private const val TOKEN_METADATA_AUTHORITY = "tokenmetadata.provider"
        private const val OWNED_TOKEN_AUTHORITY = "ownedtokens.provider"
        private const val WALLET_PKG = "org.ethereumphone.walletmanager.testing123"
    }
} 