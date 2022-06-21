package com.unitpricecalculator.comparisons

import android.os.Parcelable
import com.unitpricecalculator.unit.DefaultUnit
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedUnitEntryRow(
    val cost: String,
    val quantity: String,
    val size: String,
    val unit: DefaultUnit,
    val note: String?
): Parcelable {
    val isEmpty: Boolean
        get() = cost.isNotEmpty() || quantity.isNotEmpty() || size.isNotEmpty() ||
                !note.isNullOrEmpty()
}
