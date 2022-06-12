package com.unitpricecalculator.saved

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.unitpricecalculator.R
import com.unitpricecalculator.comparisons.SavedComparison
import com.unitpricecalculator.comparisons.SavedUnitEntryRow
import com.unitpricecalculator.time.DateTimeHelper
import com.unitpricecalculator.unit.Unit
import com.unitpricecalculator.unit.UnitEntry
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.parseDoubleOrNull

class SavedComparisonsArrayAdapter(
  context: Context,
  savedComparisons: List<SavedComparison>,
  private val units: Units
) : ArrayAdapter<SavedComparison?>(context, R.layout.row_saved, savedComparisons) {
  override fun getView(
    position: Int,
    convertView: View?,
    parent: ViewGroup
  ): View {
    val view =
      convertView ?: LayoutInflater.from(parent.context)
        .inflate(R.layout.row_saved, null)
        .also { it.tag = ViewHolder(it) }

    val comparison = getItem(position)!!
    val viewHolder = view.tag as ViewHolder

    viewHolder.title.text =
      context.resources.getQuantityString(
        R.plurals.saved_comparison_items,
        comparison.savedUnitEntryRows.size,
        comparison.name,
        comparison.savedUnitEntryRows.size
      )

    val date = comparison.timestampMillis
    if (date == null) {
      viewHolder.date.visibility = View.GONE
    } else {
      viewHolder.date.visibility = View.VISIBLE
      viewHolder.date.text = DateTimeHelper.toMonthDateString(date)
    }

    val bestRow = comparison.getBestRow()
    if (bestRow == null) {
      viewHolder.subtitle.visibility = View.INVISIBLE
      viewHolder.subtitle.text = ""
    } else {
      viewHolder.subtitle.visibility = View.VISIBLE
      viewHolder.subtitle.text =
        context.getString(R.string.saved_comparison_subtitle, bestRow.getSummaryText())
    }

    return view
  }

  private data class ViewHolder(val view: View) {
    val title = view.findViewById<TextView>(R.id.text_title)!!
    val subtitle = view.findViewById<TextView>(R.id.text_subtitle)!!
    val date = view.findViewById<TextView>(R.id.text_date)!!
  }

  private fun SavedComparison.getBestRow(): SavedUnitEntryRow? {
    val comparisonUnit = finalUnit ?: return null
    return savedUnitEntryRows.mapNotNull {
      val cost = it.cost.parseDoubleOrNull() ?: return@mapNotNull null
      val size = it.size.parseDoubleOrNull()?: return@mapNotNull null
      val quantity = it.quantity.parseDoubleOrNull() ?: 1.0
      val unit = it.unit
      val pricePerUnit = cost / (quantity * size)
      it to  pricePerUnit / (unit.factor / comparisonUnit.factor)
    }.minByOrNull { it.second }?.first
  }

  private fun SavedUnitEntryRow.getSummaryText(): String {
    val cost = this.cost.parseDoubleOrNull() ?: 1.0
    val quantity = this.quantity.parseDoubleOrNull() ?: 1.0
    val size = this.quantity.parseDoubleOrNull() ?: 1.0
    val formattedPrice = units.formatter.apply(cost)
    val rawSummary = when{
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