package com.unitpricecalculator.comparisons

import android.os.Parcelable
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.parseDoubleOrNull
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedUnitEntryRow(
    val cost: String,
    val quantity: String,
    val size: String,
    val unit: DefaultUnit,
    val note: String?
) : Parcelable {
    @IgnoredOnParcel
    val isEmpty =
        cost.isEmpty() && quantity.isEmpty() && size.isEmpty() && note.isNullOrEmpty()

}

val SavedUnitEntryRow.pricePerBaseUnit: Double?
    get() {
        val cost = cost.parseDoubleOrNull() ?: return null
        val size = size.parseDoubleOrNull() ?: return null
        val quantity = quantity.parseDoubleOrNull() ?: 1.0
        val totalAmount = quantity * size
        val pricePerUnit = cost / totalAmount
        return pricePerUnit / unit.factor
    }
