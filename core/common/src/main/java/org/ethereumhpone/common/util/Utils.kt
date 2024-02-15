package org.ethereumhpone.common.util

import timber.log.Timber
import java.text.Normalizer
import java.util.regex.Pattern

fun <T> tryOrNull(logOnError: Boolean = true, body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {
        if (logOnError) {
            Timber.w(e)
        }

        null
    }
}

fun removeAccents(input: String): String {
    val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalizedString).replaceAll("")
}