package com.example.ch12.testutil

import com.example.ch12.data.photo.CompressedImage
import com.example.ch12.data.photo.ImageCompressor
import com.example.ch12.data.photo.ImageDecodeException

// Pengganti BitmapFactory: selalu "mengompres" 4000x3000 menjadi 400x400, 40 KB
class FakeImageCompressor : ImageCompressor {

    var failureMessage: String? = null
    val compressedUris = mutableListOf<String>()

    override suspend fun compress(uri: String): CompressedImage {
        compressedUris += uri
        failureMessage?.let { throw ImageDecodeException(it) }
        return CompressedImage(
            bytes = ByteArray(40 * 1024),
            originalWidth = 4000,
            originalHeight = 3000,
            width = 400,
            height = 400
        )
    }
}
