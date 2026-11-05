package com.example.ch12.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageMathTest {

    // Contoh di buku: 4000 x 3000 x 4 byte ≈ 48 MB
    @Test fun `memori foto 12 MP sekitar 48 MB`() {
        assertEquals(48_000_000L, decodedSizeBytes(4000, 3000))
    }

    @Test fun `pertanyaan konseptual 3000x2000`() {
        assertEquals(24_000_000L, decodedSizeBytes(3000, 2000))
    }

    @Test fun `inSampleSize pangkat 2 terbesar dengan sisi pendek tetap cukup`() {
        // 3000 / 4 = 750 >= 400, 3000 / 8 = 375 < 400 -> 4
        assertEquals(4, calculateInSampleSize(4000, 3000, 400))
        assertEquals(8, calculateInSampleSize(6000, 4000, 400))
    }

    @Test fun `gambar kecil tidak di-downsample`() {
        assertEquals(1, calculateInSampleSize(640, 480, 400))
        assertEquals(1, calculateInSampleSize(200, 200, 400))
    }

    @Test fun `potong persegi di tengah gambar landscape`() {
        assertEquals(SquareCrop(left = 500, top = 0, size = 3000), centerSquare(4000, 3000))
    }

    @Test fun `potong persegi di tengah gambar portrait`() {
        assertEquals(SquareCrop(left = 0, top = 500, size = 3000), centerSquare(3000, 4000))
    }

    @Test fun `gambar persegi tidak dipotong`() {
        assertEquals(SquareCrop(0, 0, 400), centerSquare(400, 400))
    }

    @Test fun `format ukuran berkas`() {
        assertEquals("512 B", formatBytes(512))
        assertEquals("38 KB", formatBytes(38 * 1024L))
        assertEquals("45,8 MB", formatBytes(48_000_000L))
    }
}
