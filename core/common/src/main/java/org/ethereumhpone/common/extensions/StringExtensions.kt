package org.ethereumhpone.common.extensions

import java.text.Normalizer

fun CharSequence.removeAccents(): String = Normalizer.normalize(this, Normalizer.Form.NFKD).replace(Regex("\\p{M}"), "")
