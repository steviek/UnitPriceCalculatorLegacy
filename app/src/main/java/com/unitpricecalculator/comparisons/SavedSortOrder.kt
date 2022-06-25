package com.unitpricecalculator.comparisons

import com.unitpricecalculator.util.prefs.Prefs
import com.unitpricecalculator.util.prefs.PrefsKeys

enum class SavedSortOrder(val serialNumber: Int) {
    LastModifiedDescending(1),
    LastModifiedAscending(2),
    TitleDescending(3),
    TitleAscending(4);

    fun toggledByTitle() = when (this) {
        LastModifiedDescending, LastModifiedAscending, TitleDescending -> TitleAscending
        TitleAscending -> TitleDescending
    }

    fun toggledByLastModified() = when (this) {
        LastModifiedDescending -> LastModifiedAscending
        LastModifiedAscending, TitleDescending, TitleAscending -> LastModifiedDescending
    }
}

var Prefs.savedSortOrder: SavedSortOrder
    get() {
        val raw = this[PrefsKeys.SavedSortOrder]
        return SavedSortOrder.values().firstOrNull { it.serialNumber == raw }
            ?: SavedSortOrder.LastModifiedDescending
    }
    set(value) {
        this[PrefsKeys.SavedSortOrder] = value.serialNumber
    }