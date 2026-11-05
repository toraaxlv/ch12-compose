package com.example.ch12.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {
    private val now = 1_000_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour

    @Test fun `kurang dari semenit = baru saja`() =
        assertEquals("baru saja", formatRelativeTime(now, now - 30_000))

    @Test fun `waktu di masa depan tidak negatif`() =
        assertEquals("baru saja", formatRelativeTime(now, now + hour))

    @Test fun `menit`() = assertEquals("5 menit lalu", formatRelativeTime(now, now - 5 * minute))

    @Test fun `jam`() = assertEquals("3 jam lalu", formatRelativeTime(now, now - 3 * hour))

    @Test fun `hari`() = assertEquals("2 hari lalu", formatRelativeTime(now, now - 2 * day))

    @Test fun `bulan`() = assertEquals("3 bulan lalu", formatRelativeTime(now, now - 95 * day))

    @Test fun `tahun`() = assertEquals("2 tahun lalu", formatRelativeTime(now, now - 800 * day))
}
