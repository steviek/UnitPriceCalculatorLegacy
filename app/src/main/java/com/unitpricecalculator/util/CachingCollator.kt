package com.unitpricecalculator.util

import androidx.collection.SimpleArrayMap
import java.text.CollationKey
import java.text.Collator
import java.util.Locale

class CachingCollator<T>(locale: Locale, private val transform: (T) -> String) : Comparator<T> {

    private val collator = Collator.getInstance(locale)
    private val collationKeys = SimpleArrayMap<T, CollationKey>()

    override fun compare(o1: T, o2: T): Int {
        return getOrCreateCollationKey(o1).compareTo(getOrCreateCollationKey(o2))
    }

    private fun getOrCreateCollationKey(value: T): CollationKey {
        collationKeys.get(value)?.let { return it }
        val collationKey = collator.getCollationKey(transform(value))
        collationKeys.put(value, collationKey)
        return collationKey
    }
}
