package com.example.ch12.util

// Sama dengan Ch10: Hilt tidak bisa meng-inject lambda () -> Long.
// Di uji cukup Clock { 5_000L }.
fun interface Clock {
    fun now(): Long
}
