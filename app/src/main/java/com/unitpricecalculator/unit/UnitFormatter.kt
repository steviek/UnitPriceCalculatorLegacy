package com.unitpricecalculator.unit

import android.content.Context
import android.content.res.Resources
import android.icu.text.MeasureFormat
import android.icu.text.MeasureFormat.FormatWidth.WIDE
import android.icu.text.MessageFormat
import android.icu.util.Measure
import com.unitpricecalculator.R
import com.unitpricecalculator.unit.DefaultUnit.ACRE
import com.unitpricecalculator.unit.DefaultUnit.ARE
import com.unitpricecalculator.unit.DefaultUnit.CENTIMETRE
import com.unitpricecalculator.unit.DefaultUnit.CUBIC_CENTIMETRE
import com.unitpricecalculator.unit.DefaultUnit.CUP
import com.unitpricecalculator.unit.DefaultUnit.DAY
import com.unitpricecalculator.unit.DefaultUnit.DOZEN
import com.unitpricecalculator.unit.DefaultUnit.FLUID_OUNCE
import com.unitpricecalculator.unit.DefaultUnit.FOOT
import com.unitpricecalculator.unit.DefaultUnit.GALLON
import com.unitpricecalculator.unit.DefaultUnit.GRAM
import com.unitpricecalculator.unit.DefaultUnit.HECTARE
import com.unitpricecalculator.unit.DefaultUnit.HOUR
import com.unitpricecalculator.unit.DefaultUnit.INCH
import com.unitpricecalculator.unit.DefaultUnit.KILOGRAM
import com.unitpricecalculator.unit.DefaultUnit.KILOMETRE
import com.unitpricecalculator.unit.DefaultUnit.LITRE
import com.unitpricecalculator.unit.DefaultUnit.METRE
import com.unitpricecalculator.unit.DefaultUnit.MILE
import com.unitpricecalculator.unit.DefaultUnit.MILLIGRAM
import com.unitpricecalculator.unit.DefaultUnit.MILLILITRE
import com.unitpricecalculator.unit.DefaultUnit.MILLIMETRE
import com.unitpricecalculator.unit.DefaultUnit.MINUTE
import com.unitpricecalculator.unit.DefaultUnit.MONTH
import com.unitpricecalculator.unit.DefaultUnit.OUNCE
import com.unitpricecalculator.unit.DefaultUnit.PINT
import com.unitpricecalculator.unit.DefaultUnit.POUND
import com.unitpricecalculator.unit.DefaultUnit.QUART
import com.unitpricecalculator.unit.DefaultUnit.SECOND
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_CENTIMETRE
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_FOOT
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_INCH
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_KILOMETRE
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_METRE
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_MILE
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_MILLIMETRE
import com.unitpricecalculator.unit.DefaultUnit.SQUARE_YARD
import com.unitpricecalculator.unit.DefaultUnit.STONE
import com.unitpricecalculator.unit.DefaultUnit.TABLESPOON
import com.unitpricecalculator.unit.DefaultUnit.TEASPOON
import com.unitpricecalculator.unit.DefaultUnit.UNIT
import com.unitpricecalculator.unit.DefaultUnit.US_CUP
import com.unitpricecalculator.unit.DefaultUnit.US_FLUID_OUNCE
import com.unitpricecalculator.unit.DefaultUnit.US_GALLON
import com.unitpricecalculator.unit.DefaultUnit.US_PINT
import com.unitpricecalculator.unit.DefaultUnit.US_QUART
import com.unitpricecalculator.unit.DefaultUnit.US_TABLESPOON
import com.unitpricecalculator.unit.DefaultUnit.US_TEASPOON
import com.unitpricecalculator.unit.DefaultUnit.WEEK
import com.unitpricecalculator.unit.DefaultUnit.YARD
import com.unitpricecalculator.unit.DefaultUnit.YEAR
import com.unitpricecalculator.unit.MeasureUnitOrPlural.Plural
import com.unitpricecalculator.unit.UnitType.AREA
import com.unitpricecalculator.unit.UnitType.LENGTH
import com.unitpricecalculator.unit.UnitType.QUANTITY
import com.unitpricecalculator.unit.UnitType.TIME
import com.unitpricecalculator.unit.UnitType.VOLUME
import com.unitpricecalculator.unit.UnitType.WEIGHT
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class UnitFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun format(
        unit: DefaultUnit,
        size: Double,
        sizeString: String,
    ): String {
        val formatResId = when (unit) {
            GRAM, KILOGRAM, MILLIGRAM, OUNCE, POUND, STONE, MILLILITRE, LITRE, CUBIC_CENTIMETRE,
            GALLON, QUART, PINT, CUP, FLUID_OUNCE, TABLESPOON, TEASPOON, US_GALLON, US_QUART,
            US_PINT, US_CUP, US_FLUID_OUNCE, US_TABLESPOON, US_TEASPOON, METRE, KILOMETRE,
            CENTIMETRE, MILLIMETRE, INCH, FOOT, YARD, MILE, SQUARE_METRE, SQUARE_CENTIMETRE,
            SQUARE_MILLIMETRE, SQUARE_KILOMETRE, ARE, HECTARE, ACRE, SQUARE_FOOT, SQUARE_INCH,
            SQUARE_YARD, SQUARE_MILE -> {
                val symbol = unit.getSymbol(context.resources)
                return if (size == 1.0) {
                    symbol
                } else {
                    "$sizeString $symbol"
                }
            }
            DAY -> R.string.day_name
            WEEK -> R.string.week_name
            MONTH -> R.string.month_name
            YEAR -> R.string.year_name
            SECOND -> R.string.second_name
            MINUTE -> R.string.minute_name
            HOUR -> R.string.hour_name
            UNIT -> R.string.unit_name
            DOZEN -> R.string.dozen_name
        }
        val format = context.getString(formatResId)
        return MessageFormat.format(format, mapOf("count" to size))
    }
}