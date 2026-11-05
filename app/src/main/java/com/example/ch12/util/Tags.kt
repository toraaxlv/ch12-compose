package com.example.ch12.util

// "Kuliah, #Ide,  kuliah" -> ["kuliah", "ide"]
// Dipisah koma; spasi dan awalan # dibuang; huruf kecil; tanpa duplikat.
fun parseTags(text: String): List<String> =
    text.split(',')
        .map { it.trim().removePrefix("#").trim().lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()

fun formatTags(tags: List<String>): String = tags.joinToString(", ")
