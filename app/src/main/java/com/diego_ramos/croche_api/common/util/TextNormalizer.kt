package com.diego_ramos.croche_api.common.util

object TextNormalizer {

    private val particles = setOf(
        "de", "da", "do", "das", "dos", "e"
    )

    fun normalizeName(name: String): String {
        if (name.isBlank()) {
            return ""
        }

        val words = name.trim().split(Regex("\\s+"))

        return words.mapIndexed { index, word ->
            val normalized = word.lowercase()

            if (index > 0 && normalized in particles) {
                normalized
            } else {
                normalized.replaceFirstChar { it.uppercase() }
            }
        }.joinToString(" ")
    }

    fun normalizeEmail(value: String): String {
        return value.trim().lowercase()
    }
}