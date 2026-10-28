package com.diego_ramos.croche_api.common.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val localePtBr = Locale.forLanguageTag("pt-BR")
    private val currencyFormat = NumberFormat.getCurrencyInstance(localePtBr)
    private val numberFormat = NumberFormat.getNumberInstance(localePtBr).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun format(amount: BigDecimal): String {
        return currencyFormat.format(amount)
    }

    fun formatCentsWithoutSymbol(cents: Long): String {
        val amount = centsToBigDecimal(cents)
        return numberFormat.format(amount)
    }

    fun centsToBigDecimal(cents: Long): BigDecimal {
        return BigDecimal(cents).movePointLeft(2)
    }

    fun bigDecimalToCents(amount: BigDecimal?): Long {
        if (amount == null) return 0L
        return amount.movePointRight(2).toLong()
    }
}
