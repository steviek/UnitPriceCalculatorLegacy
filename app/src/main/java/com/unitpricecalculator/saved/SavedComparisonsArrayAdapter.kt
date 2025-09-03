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
import com.unitpricecalculator.comparisons.pricePerBaseUnit
import com.unitpricecalculator.time.DateTimeHelper
import com.unitpricecalculator.unit.Unit
import com.unitpricecalculator.unit.UnitEntry
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.util.minByOrNullNotNull
import com.unitpricecalculator.util.parseDoubleOrNull
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.Currency

class SavedComparisonsArrayAdapter @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted savedComparisons: List<SavedComparison>,
    private val formatter: SavedComparisonFormatter,
) : ArrayAdapter<SavedComparison?>(context, R.layout.row_saved, savedComparisons) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view =
            convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.row_saved, parent, false)
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
            viewHolder.subtitle.text = formatter.formatSummary(bestRow, comparison.currency)
        }

        return view
    }

    private data class ViewHolder(val view: View) {
        val title = view.findViewById<TextView>(R.id.text_title)!!
        val subtitle = view.findViewById<TextView>(R.id.text_subtitle)!!
        val date = view.findViewById<TextView>(R.id.text_date)!!
    }

    private fun SavedComparison.getBestRow(): SavedUnitEntryRow? {
        return savedUnitEntryRows.minByOrNullNotNull { it.pricePerBaseUnit }
    }

}

@AssistedFactory
interface SavedComparisonsArrayAdapterFactory {
    fun create(
        context: Context,
        savedComparisons: List<SavedComparison>
    ): SavedComparisonsArrayAdapter
}
