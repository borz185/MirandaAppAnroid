package com.miranda.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    // Список всех возможных форматов, которые может прислать бэкенд
    private val inputFormats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",          // Стандартный ISO с Z
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",      // ISO с миллисекундами
        "yyyy-MM-dd HH:mm:ss",               // Стандартный SQL datetime
        "yyyy-MM-dd"                         // Просто дата
    )

    // Универсальный метод парсинга
    private fun parseDate(dateString: String): Date? {
        for (format in inputFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.isLenient = false // Строгая проверка формата
                return sdf.parse(dateString)
            } catch (e: Exception) {
                // Если формат не подошел, игнорируем ошибку и пробуем следующий
                continue
            }
        }
        return null // Если ни один формат не подошел
    }

    // Формат: 18.07.2026
    fun formatDateShort(dateString: String): String {
        val date = parseDate(dateString)
        return if (date != null) {
            SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(date)
        } else {
            dateString // Возвращаем как есть, если не смогли распарсить
        }
    }

    // Формат: 18.07.2026 14:30 (для платежей)
    fun formatDateWithTime(dateString: String): String {
        val date = parseDate(dateString)
        return if (date != null) {
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(date)
        } else {
            dateString
        }
    }

    // Формат: 18 августа 2026 (для тарифов и профиля)
    fun formatDateLong(dateString: String): String {
        val date = parseDate(dateString)
        return if (date != null) {
            SimpleDateFormat("dd MMMM yyyy", Locale("ru")).format(date)
        } else {
            dateString
        }
    }

    // Формат: 18 августа 2026, 14:30
    fun formatDateFull(dateString: String): String {
        val date = parseDate(dateString)
        return if (date != null) {
            SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru")).format(date)
        } else {
            dateString
        }
    }
}