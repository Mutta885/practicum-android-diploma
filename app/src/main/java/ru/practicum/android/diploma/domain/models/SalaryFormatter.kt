package ru.practicum.android.diploma.domain.models

import java.util.Currency
import java.util.Locale

object SalaryFormatter {
    fun getFormattedSalary(salary: Salary): String {
        return when {
            salary.from != null && salary.to != null ->
                "от ${formatNumber(salary.from)} до ${formatNumber(salary.to)} ${getCurrencySymbol(salary.currency.toString())}"

            salary.from != null ->
                "от ${formatNumber(salary.from)} ${getCurrencySymbol(salary.currency.toString())}"

            salary.to != null ->
                "до ${formatNumber(salary.to)} ${getCurrencySymbol(salary.currency.toString())}"

            else -> "Зарплата не указана"
        }.trim()
    }

    private fun formatNumber(number: Int): String {
        return String.format(Locale.getDefault(), "%,d", number).replace(',', ' ')
    }

    private fun getCurrencySymbol(currencyCode: String, locale: Locale = Locale.getDefault()): String {
        var currencySymbol = ""
        currencySymbol = when (currencyCode) {
            "RUB" -> "₽"
            "SEK" -> "KR"
            "SGD" -> "S$"
            else -> Currency.getInstance(currencyCode).getSymbol(locale)
        }
        return currencySymbol
    }
}
