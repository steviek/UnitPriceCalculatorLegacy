package com.unitpricecalculator.comparisons

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComparisonFragmentState(
    val currentComparison: SavedComparison,
    val lastKnownSavedComparison: SavedComparison? = null
): Parcelable
