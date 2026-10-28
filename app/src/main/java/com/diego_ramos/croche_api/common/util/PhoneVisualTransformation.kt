package com.diego_ramos.croche_api.common.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter { it.isDigit() }.take(11)

        val formatted = buildString {
            for (i in raw.indices) {
                if (i == 0) append("(")
                if (i == 2) append(") ")
                if (raw.length <= 10) {
                    if (i == 6) append("-")
                } else {
                    if (i == 7) append("-")
                }
                append(raw[i])
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val digitsBefore = raw.take(offset.coerceIn(0, raw.length)).length
                if (digitsBefore == 0) return 0
                if (digitsBefore <= 2) return digitsBefore + 1 // "("
                if (digitsBefore <= if (raw.length <= 10) 6 else 7) return digitsBefore + 3 // "(XX) "
                return digitsBefore + 4 // "(XX) XXXXX-"
            }

            override fun transformedToOriginal(offset: Int): Int {
                var digits = 0
                for (i in 0 until offset.coerceAtMost(formatted.length)) {
                    if (formatted[i].isDigit()) digits++
                }
                return digits.coerceAtMost(raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    companion object {
        fun format(phone: String?): String {
            if (phone.isNullOrBlank()) return ""
            val raw = phone.filter { it.isDigit() }.take(11)
            if (raw.isEmpty()) return ""
            return buildString {
                for (i in raw.indices) {
                    if (i == 0) append("(")
                    if (i == 2) append(") ")
                    if (raw.length <= 10) {
                        if (i == 6) append("-")
                    } else {
                        if (i == 7) append("-")
                    }
                    append(raw[i])
                }
            }
        }
    }
}
