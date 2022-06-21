package com.unitpricecalculator.comparisons

import android.os.Parcelable
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@Parcelize
data class Comparison(
    val key: String,
    val title: String?,
    val unitType: UnitType,
    val rows: List<UnitEntryRow>,
    val finalQuantity: String?,
    val finalUnit: DefaultUnit,
    val currencyCode: String,
    val lastSavedTimestampMs: Long?
): Parcelable

@Parcelize
data class UnitEntryRow(
    val cost: String?,
    val quantity: String?,
    val size: String?,
    val unit: DefaultUnit,
    val note: String?
): Parcelable

private var lastKeyGenerated: Long? = null

class ComparisonFactory @Inject constructor(private val units: Units) {

    fun createComparison(): SavedComparison {
        return SavedComparison(
            _key = generateKey(),
            name = "",
            unitType = units.currentUnitType,
            savedUnitEntryRows = (0 until 2).map { createRow() },
            finalQuantity = "",
            finalUnit = units.getDefaultQuantity().unit,
            currencyCode = units.currency.currencyCode,
            timestampMillis = null
        )
    }

    fun createRow(): SavedUnitEntryRow {
        return SavedUnitEntryRow(
            cost = "",
            quantity = "",
            size = "",
            unit = units.getDefaultQuantity().unit,
            note = null
        )
    }
}

private fun generateKey(): String {
    var currentTime = System.currentTimeMillis()
    val lastTimeUsed = lastKeyGenerated
    if (lastTimeUsed != null && currentTime <= lastTimeUsed) {
        currentTime = lastTimeUsed + 1
    }
    lastKeyGenerated = currentTime
    return currentTime.toString()
}