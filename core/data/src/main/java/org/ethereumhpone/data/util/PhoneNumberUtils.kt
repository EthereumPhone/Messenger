package org.ethereumhpone.data.util

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PhoneNumberUtils @Inject constructor(context: Context) {
    private val countryCode = Locale.getDefault().country
    private val phoneNumberUtil = PhoneNumberUtil.createInstance(context)
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    fun compare(first: String, second: String): Boolean {
        if (first.equals(second, true)) {
            return true
        }

        if (PhoneNumberUtils.areSamePhoneNumber(first, second, telephonyManager.networkCountryIso)) {
            val matchType = phoneNumberUtil.isNumberMatch(first, second)
            if (matchType >= PhoneNumberUtil.MatchType.SHORT_NSN_MATCH) {
                return true
            }
        }
        return false
    }

    fun isPossibleNumber(number: CharSequence): Boolean {
        return parse(number) != null
    }

    fun isReallyDialable(digit: Char): Boolean {
        return PhoneNumberUtils.isReallyDialable(digit)
    }

    fun formatNumber(number: CharSequence): String {
        // PhoneNumberUtil doesn't maintain country code input
        return PhoneNumberUtils.formatNumber(number.toString(), countryCode) ?: number.toString()
    }

    fun normalizeNumber(number: String): String {
        return PhoneNumberUtils.stripSeparators(number)
    }

    private fun parse(number: CharSequence): Phonenumber.PhoneNumber? {
        return try {
            phoneNumberUtil.parse(number, countryCode)
        } catch (e: Exception) {
            null
        }
    }
}