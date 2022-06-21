package com.unitpricecalculator.util.sometimes

inline fun <T> Sometimes<T>.ifPresent(block: (T) -> Unit) {
    orNull()?.let(block)
}