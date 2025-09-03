package com.unitpricecalculator.saved

import android.content.Context
import com.unitpricecalculator.R
import com.unitpricecalculator.comparisons.SavedUnitEntryRow
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.parseDoubleOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Currency
import javax.inject.Inject

class SavedComparisonFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val units: Units,
) {
    fun formatSummary(bestRow: SavedUnitEntryRow, currency: Currency?) : String {
        return context.getString(
            R.string.saved_comparison_subtitle,
            bestRow.getSummaryText(currency)
        )
    }

    private fun SavedUnitEntryRow.getSummaryText(currency: Currency?): String {
        val cost = this.cost.parseDoubleOrNull() ?: 1.0
        val quantity = this.quantity.parseDoubleOrNull() ?: 1.0
        val size = this.size.parseDoubleOrNull() ?: 1.0
        val formattedPrice = units.getCostFormatter(currency).format(cost)
        val rawSummary = when {
            quantity == 1.0 && size == 1.0 -> {
                context.getString(
                    R.string.m_per_u,
                    formattedPrice,
                    unit.getSymbol(context.resources)
                )
            }

            quantity == 1.0 -> {
                context.getString(
                    R.string.m_per_s_u,
                    formattedPrice,
                    this.size,
                    unit.getSymbol(context.resources)
                )
            }

            else -> {
                context.getString(
                    R.string.m_per_qxs_u,
                    formattedPrice,
                    this.quantity,
                    this.size,
                    unit.getSymbol(context.resources)
                )
            }
        }
        return if (note.isNullOrBlank()) {
            rawSummary
        } else {
            "$note ($rawSummary)"
        }
    }
}