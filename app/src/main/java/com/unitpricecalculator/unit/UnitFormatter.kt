package com.unitpricecalculator.unit

import android.content.res.Resources
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth.WIDE
import android.icu.util.Measure
import android.os.Build.VERSION
import com.unitpricecalculator.R
import com.unitpricecalculator.util.isIntegral
import com.unitpricecalculator.util.parseDoubleOrNull
import java.util.Locale

object UnitFormatter {
  fun format(quantity: String, resources: Resources, unit: Unit): String {
    val number = quantity.parseDoubleOrNull()
    if (VERSION.SDK_INT < 24 || number == null) {
      return formatWithSymbol(quantity, resources, unit)
    }

    val measureUnit = unit.toMeasureUnit()
    if (measureUnit != null) {
      return MeasureFormat.getInstance(Locale.getDefault(), WIDE)
        .formatMeasures(Measure(number, measureUnit))
    }

    when (unit) {
      DefaultUnit.DOZEN -> formatWithName(quantity, resources.getString(R.string.dozen_name))
      DefaultUnit.UNIT -> {
        formatWithName(quantity, resources.getQuantityString(R.plurals.unit_name, number.toInt()))
      }
      DefaultUnit.ARE -> {
        formatWithName(quantity, resources.getQuantityString(R.plurals.are_name, number.toInt()))
      }
      DefaultUnit.SQUARE_MILLIMETRE -> {
        formatWithName(
          quantity,
          resources.getQuantityString(R.plurals.square_millimetre_name, number.toInt())
        )
      }
    }


    return formatWithSymbol(quantity, resources, unit)
  }

  private fun formatWithSymbol(quantity: String, resources: Resources, unit: Unit): String {
    return formatWithName(quantity, unit.getSymbol(resources))
  }

  private fun formatWithName(quantity: String, name: String): String {
    return if (quantity.parseDoubleOrNull()?.isIntegral() == true) {
      name
    } else {
      "$quantity $name"
    }
  }
}