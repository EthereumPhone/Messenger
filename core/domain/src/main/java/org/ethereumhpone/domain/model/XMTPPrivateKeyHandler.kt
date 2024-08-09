package org.ethereumhpone.domain.model
import android.content.SharedPreferences

class XMTPPrivateKeyHandler(
    private val sharedPreferences: SharedPreferences
) {
    fun getPrivate(): String? = sharedPreferences.getString("xmtpKey", null)

    fun setPrivate(privateKey: String) {
        sharedPreferences.edit().putString("xmtpKey", privateKey).commit()
    }
}