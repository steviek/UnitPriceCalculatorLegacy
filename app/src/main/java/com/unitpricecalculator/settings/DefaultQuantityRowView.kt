package com.unitpricecalculator.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.unitpricecalculator.R
import com.unitpricecalculator.unit.Quantity
import com.unitpricecalculator.util.toLocalizedString

class DefaultQuantityRowView(
  context: Context,
  attrs: AttributeSet
) : FrameLayout(context, attrs) {

  private val summaryTextView: TextView

  init {
    LayoutInflater.from(context).inflate(R.layout.view_default_quantity_row_contents, this)
    summaryTextView = findViewById(R.id.summary)
  }

  fun setData(quantity: Quantity) {
    val unitTypeName = resources.getString(quantity.unit.unitType.getName())
    val quantityAmount = quantity.amount.toLocalizedString()
    val quantitySymbol = quantity.unit.getSymbol(resources)
    summaryTextView.text =
      resources.getString(
        R.string.default_unit_quantity_summary_line,
        unitTypeName,
        quantityAmount,
        quantitySymbol
      )
  }

}
