package org.ethereumhpone.data.manager

import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.xmtp.android.library.SignedData
import org.xmtp.android.library.SignerType
import org.xmtp.android.library.SigningKey
import org.xmtp.android.library.libxmtp.IdentityKind
import org.xmtp.android.library.libxmtp.PublicIdentity

/**
 * A SigningKey implementation backed by a locally generated secp256k1 keypair.
 * Used for creating isolated XMTP identities for third-party apps.
 */
class GeneratedWallet(private val ecKeyPair: ECKeyPair) : SigningKey {

    val address: String = "0x" + Keys.getAddress(ecKeyPair)

    override val publicIdentity: PublicIdentity
        get() = PublicIdentity(IdentityKind.ETHEREUM, address)

    override val type: SignerType
        get() = SignerType.EOA

    override var chainId: Long? = null

    override var blockNumber: Long? = null

    override suspend fun sign(message: String): SignedData {
        val signData = Sign.signPrefixedMessage(
            message.toByteArray(Charsets.UTF_8),
            ecKeyPair
        )
        val signatureBytes = ByteArray(65)
        System.arraycopy(signData.r, 0, signatureBytes, 0, 32)
        System.arraycopy(signData.s, 0, signatureBytes, 32, 32)
        signatureBytes[64] = signData.v[0]
        return SignedData(signatureBytes)
    }

    companion object {
        /** Reconstructs a GeneratedWallet from a hex-encoded private key. */
        fun fromPrivateKeyHex(hex: String): GeneratedWallet {
            val keyPair = ECKeyPair.create(java.math.BigInteger(hex, 16))
            return GeneratedWallet(keyPair)
        }
    }
}
