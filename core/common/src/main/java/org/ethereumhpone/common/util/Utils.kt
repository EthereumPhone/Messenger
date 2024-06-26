package org.ethereumhpone.common.util

import timber.log.Timber
import java.text.Normalizer
import java.util.regex.Pattern

fun <T> tryOrNull(body: () -> T?): T? {
    return try {
        body()
    } catch (e: Exception) {

        e.printStackTrace()
        null
    }
}

fun removeAccents(input: String): String {
    val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalizedString).replaceAll("")
}