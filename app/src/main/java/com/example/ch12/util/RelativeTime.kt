package com.example.ch12.util

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val MONTH = 30 * DAY
private const val YEAR = 365 * DAY

// Fungsi murni (tanpa Android): mudah diuji di JVM.
// Contoh: 3 jam lalu -> "3 jam lalu"; kurang dari semenit -> "baru saja".
fun formatRelativeTime(now: Long, then: Long): String {
    val diff = (now - then).coerceAtLeast(0)
    return when {
        diff < MINUTE -> "baru saja"
        diff < HOUR -> "${diff / MINUTE} menit lalu"
        diff < DAY -> "${diff / HOUR} jam lalu"
        diff < MONTH -> "${diff / DAY} hari lalu"
        diff < YEAR -> "${diff / MONTH} bulan lalu"
        else -> "${diff / YEAR} tahun lalu"
    }
}
