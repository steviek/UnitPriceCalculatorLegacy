package com.unitpricecalculator.comparisons

import android.os.Parcelable
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.util.logger.Logger
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
