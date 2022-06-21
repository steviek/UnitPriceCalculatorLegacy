package com.unitpricecalculator.util

inline fun <T> nullsLastThen(
    crossinline comparator: (o1: T, o2: T) -> Int
) = Comparator<T> { o1, o2 ->
    when {
        o1 == null && o2 == null -> 0
        o1 == null -> 1
        o2 == null -> -1
        else -> comparator(o1, o2)
    }
}
