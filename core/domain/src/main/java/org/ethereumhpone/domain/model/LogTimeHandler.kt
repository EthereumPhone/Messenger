package org.ethereumhpone.domain.model

import android.content.SharedPreferences

class LogTimeHandler(
    private val sharedPreferences: SharedPreferences
) {
    fun getLastLog(): Long = sharedPreferences.getLong("lastLog", 0)

    fun setLastLog(time: Long) {
        sharedPreferences.edit().putLong("lastLog", time).apply()
    }

}