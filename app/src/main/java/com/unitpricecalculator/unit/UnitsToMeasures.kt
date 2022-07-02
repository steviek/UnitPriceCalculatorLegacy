package com.unitpricecalculator.unit

import android.annotation.TargetApi
import android.icu.util.MeasureUnit
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.unitpricecalculator.R
import com.unitpricecalculator.R.string
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
import com.unitpricecalculator.unit.MeasureUnitOrPlural.Unit

fun DefaultUnit.toMeasureUnitOrPlural(): MeasureUnitOrPlural = when (this) {
    GRAM -> Unit(MeasureUnit.GRAM)
    KILOGRAM -> Unit(MeasureUnit.KILOGRAM)
    MILLIGRAM -> Unit(MeasureUnit.MILLIGRAM)
    OUNCE -> Unit(MeasureUnit.OUNCE)
    POUND -> Unit(MeasureUnit.POUND)
    STONE -> Unit(MeasureUnit.STONE)
    MILLILITRE -> Unit(MeasureUnit.MILLILITER)
    LITRE -> Unit(MeasureUnit.LITER)
    CUBIC_CENTIMETRE -> Unit(MeasureUnit.CUBIC_CENTIMETER)
    METRE -> Unit(MeasureUnit.METER)
    KILOMETRE -> Unit(MeasureUnit.KILOMETER)
    CENTIMETRE -> Unit(MeasureUnit.CENTIMETER)
    MILLIMETRE -> Unit(MeasureUnit.MILLIMETER)
    INCH -> Unit(MeasureUnit.INCH)
    FOOT -> Unit(MeasureUnit.FOOT)
    YARD -> Unit(MeasureUnit.YARD)
    MILE -> Unit(MeasureUnit.MILE)
    SQUARE_METRE -> Unit(MeasureUnit.SQUARE_METER)
    SQUARE_CENTIMETRE -> Unit(MeasureUnit.SQUARE_CENTIMETER)
    SQUARE_KILOMETRE -> Unit(MeasureUnit.SQUARE_KILOMETER)
    HECTARE -> Unit(MeasureUnit.HECTARE)
    ACRE -> Unit(MeasureUnit.ACRE)
    SQUARE_FOOT -> Unit(MeasureUnit.SQUARE_FOOT)
    SQUARE_INCH -> Unit(MeasureUnit.SQUARE_INCH)
    SQUARE_YARD -> Unit(MeasureUnit.SQUARE_YARD)
    SQUARE_MILE -> Unit(MeasureUnit.SQUARE_MILE)
    US_CUP, CUP -> Unit(MeasureUnit.CUP)
    US_GALLON -> Unit(MeasureUnit.GALLON)
    US_PINT, PINT -> Unit(MeasureUnit.PINT)
    QUART, US_QUART -> Unit(MeasureUnit.QUART)
    FLUID_OUNCE, US_FLUID_OUNCE -> Unit(MeasureUnit.FLUID_OUNCE)
    TEASPOON, US_TEASPOON -> Unit(MeasureUnit.TEASPOON)
    TABLESPOON, US_TABLESPOON -> Unit(MeasureUnit.TABLESPOON)
    GALLON -> Unit(if (VERSION.SDK_INT >= 28) MeasureUnit.GALLON_IMPERIAL else MeasureUnit.GALLON)
    DAY -> Unit(MeasureUnit.DAY)
    WEEK -> Unit(MeasureUnit.WEEK)
    MONTH -> Unit(MeasureUnit.MONTH)
    YEAR -> Unit(MeasureUnit.YEAR)
    SECOND -> Unit(MeasureUnit.SECOND)
    MINUTE -> Unit(MeasureUnit.MINUTE)
    HOUR -> Unit(MeasureUnit.HOUR)
    UNIT -> Plural(string.unit_name)
    DOZEN -> Plural(string.dozen_name)
    SQUARE_MILLIMETRE -> Plural(string.square_millimetre_name)
    ARE -> Plural(string.are_name)
}

sealed interface MeasureUnitOrPlural {
    data class Unit(val unit: MeasureUnit) : MeasureUnitOrPlural
    data class Plural(@StringRes val resId: Int) : MeasureUnitOrPlural
}