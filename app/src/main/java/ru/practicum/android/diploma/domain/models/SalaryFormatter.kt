package ru.practicum.android.diploma.domain.models

import java.util.Currency
import java.util.Locale

object SalaryFormatter {
    fun getFormattedSalary(salary: Salary): String {
        return when {
            salary.from != null && salary.to != null -> {
                val from = formatNumber(salary.from)
                val to = formatNumber(salary.to)
                val currency = getCurrencySymbol(salary.currency.toString())
                "от $from до $to $currency"
            }

            salary.from != null -> {
                val from = formatNumber(salary.from)
                val currency = getCurrencySymbol(salary.currency.toString())
                "от $from $currency"
            }

            salary.to != null -> {
                val to = formatNumber(salary.to)
                val currency = getCurrencySymbol(salary.currency.toString())
                "до $to $currency"
            }

            else -> "Зарплата не указана"
        }.trim()
    }

    private fun formatNumber(number: Int): String {
        return String.format(Locale.getDefault(), "%,d", number).replace(',', ' ')
    }

    private fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.getDefault()): String {
        return when (currencyCode) {
            "RUB" -> "₽"
            "SEK" -> "KR"
            "SGD" -> "S$"
            else -> Currency.getInstance(currencyCode).getSymbol(locale)
        }
    }
}
