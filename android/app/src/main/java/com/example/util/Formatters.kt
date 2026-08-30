package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val indonesianLocale = Locale("id", "ID")
    private val currencyFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
    }

    fun formatRupiah(amount: Long): String {
        return try {
            currencyFormat.format(amount).replace(",00", "")
        } catch (_: Exception) {
            "Rp $amount"
        }
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "-"
        }
    }

    fun formatDateShort(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", indonesianLocale)
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "-"
        }
    }

    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        val name = parts[0]
        val domain = parts[1]
        val maskedName = if (name.length > 3) {
            name.substring(0, 2) + "***" + name.takeLast(1)
        } else {
            name + "***"
        }
        return "$maskedName@$domain"
    }

    fun maskPassword(password: String): String {
        if (password.length <= 2) return "••••••"
        return password.take(2) + "•".repeat(password.length.coerceAtLeast(6) - 2)
    }
}
