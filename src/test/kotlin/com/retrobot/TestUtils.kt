package com.retrobot

fun String.containsAny(items: List<String>, ignoreCase: Boolean = false): Boolean {
    for (item in items) {
        if (this.contains(item, ignoreCase)) {
            return true
        }
    }
    return false
}

fun String.containsEvery(items: List<String>, ignoreCase: Boolean = false): Boolean {
    for (item in items) {
        if (!this.contains(item, ignoreCase)) {
            return false
        }
    }
    return true
}