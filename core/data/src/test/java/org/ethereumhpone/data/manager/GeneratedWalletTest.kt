package org.ethereumhpone.data.manager

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.xmtp.android.library.SignerType
import org.xmtp.android.library.libxmtp.IdentityKind
import java.math.BigInteger

class GeneratedWalletTest {

    @Test
    fun `constructor produces valid checksummed ethereum address`() {
        val keyPair = Keys.createEcKeyPair()
        val wallet = GeneratedWallet(keyPair)

        assertTrue("Address must start with 0x", wallet.address.startsWith("0x"))
        assertEquals("Address must be 42 characters", 42, wallet.address.length)
        assertTrue(
            "Address must be hex after 0x prefix",
            wallet.address.removePrefix("0x").matches(Regex("[0-9a-fA-F]{40}"))
        )
    }

    @Test
    fun `address matches web3j derivation`() {
        val keyPair = Keys.createEcKeyPair()
        val wallet = GeneratedWallet(keyPair)
        val expected = "0x" + Keys.getAddress(keyPair)

        assertEquals(expected.lowercase(), wallet.address.lowercase())
    }

    @Test
    fun `fromPrivateKeyHex roundtrips correctly`() {
        val keyPair = Keys.createEcKeyPair()
        val hex = keyPair.privateKey.toString(16)

        val restored = GeneratedWallet.fromPrivateKeyHex(hex)
        val original = GeneratedWallet(keyPair)

        assertEquals(
            "Restored address must match original",
            original.address.lowercase(),
            restored.address.lowercase()
        )
    }

    @Test
    fun `fromPrivateKeyHex with known key produces deterministic address`() {
        // Fixed private key for deterministic test
        val privateKeyHex = "4c0883a69102937d6231471b5dbb6204fe512961708279f16a9e8e0e1b8b6e7d"
        val wallet1 = GeneratedWallet.fromPrivateKeyHex(privateKeyHex)
        val wallet2 = GeneratedWallet.fromPrivateKeyHex(privateKeyHex)

        assertEquals(wallet1.address, wallet2.address)
        assertTrue(wallet1.address.startsWith("0x"))
    }

    @Test
    fun `two different keys produce different addresses`() {
        val wallet1 = GeneratedWallet(Keys.createEcKeyPair())
        val wallet2 = GeneratedWallet(Keys.createEcKeyPair())

        assertNotEquals(
            "Different keys should produce different addresses",
            wallet1.address.lowercase(),
            wallet2.address.lowercase()
        )
    }

    @Test
    fun `sign produces 65 byte signature`() = runBlocking {
        val wallet = GeneratedWallet(Keys.createEcKeyPair())
        val signedData = wallet.sign("Hello XMTP")
        val bytes = signedData.rawData

        assertEquals("Signature must be 65 bytes (r=32 + s=32 + v=1)", 65, bytes.size)
    }

    @Test
    fun `sign produces recoverable signature`() = runBlocking {
        val keyPair = Keys.createEcKeyPair()
        val wallet = GeneratedWallet(keyPair)
        val message = "XMTP : Create Identity"

        val signedData = wallet.sign(message)
        val sigBytes = signedData.rawData

        // Extract r, s, v
        val r = ByteArray(32)
        val s = ByteArray(32)
        System.arraycopy(sigBytes, 0, r, 0, 32)
        System.arraycopy(sigBytes, 32, s, 0, 32)
        val v = sigBytes[64]

        val signatureData = Sign.SignatureData(byteArrayOf(v), r, s)

        // Recover the public key from the prefixed message signature
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val prefix = "\u0019Ethereum Signed Message:\n${messageBytes.size}".toByteArray(Charsets.UTF_8)
        val prefixedHash = org.web3j.crypto.Hash.sha3(prefix + messageBytes)

        val recoveredKey = Sign.recoverFromSignature(
            (v - 27).toInt(),
            org.web3j.crypto.ECDSASignature(BigInteger(1, r), BigInteger(1, s)),
            prefixedHash
        )

        val recoveredAddress = "0x" + Keys.getAddress(recoveredKey)
        assertEquals(
            "Recovered address must match wallet address",
            wallet.address.lowercase(),
            recoveredAddress.lowercase()
        )
    }

    @Test
    fun `different messages produce different signatures`() = runBlocking {
        val wallet = GeneratedWallet(Keys.createEcKeyPair())
        val sig1 = wallet.sign("message one").rawData
        val sig2 = wallet.sign("message two").rawData

        assertFalse("Different messages must produce different signatures", sig1.contentEquals(sig2))
    }

    @Test
    fun `publicIdentity returns correct kind and address`() {
        val keyPair = Keys.createEcKeyPair()
        val wallet = GeneratedWallet(keyPair)
        val identity = wallet.publicIdentity

        assertEquals(IdentityKind.ETHEREUM, identity.kind)
        assertEquals(wallet.address, identity.identifier)
    }

    @Test
    fun `signer type is EOA`() {
        val wallet = GeneratedWallet(Keys.createEcKeyPair())
        assertEquals(SignerType.EOA, wallet.type)
    }

    @Test
    fun `chainId is null by default`() {
        val wallet = GeneratedWallet(Keys.createEcKeyPair())
        assertNull(wallet.chainId)
    }

    @Test
    fun `blockNumber is null by default`() {
        val wallet = GeneratedWallet(Keys.createEcKeyPair())
        assertNull(wallet.blockNumber)
    }
}
