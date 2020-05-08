package com.unitpricecalculator.unit

import android.annotation.TargetApi
import android.icu.util.MeasureUnit
import android.os.Build
import android.os.Build.VERSION

@TargetApi(24)
fun Unit.toMeasureUnit(): MeasureUnit? {
    if (this !is DefaultUnit) {
        return null
    }

    when (this) {
        DefaultUnit.GRAM -> return MeasureUnit.GRAM
        DefaultUnit.KILOGRAM -> return MeasureUnit.KILOGRAM
        DefaultUnit.MILLIGRAM -> return MeasureUnit.MILLIGRAM
        DefaultUnit.OUNCE -> return MeasureUnit.OUNCE
        DefaultUnit.POUND -> return MeasureUnit.POUND
        DefaultUnit.STONE -> return MeasureUnit.STONE
        DefaultUnit.MILLILITRE -> return MeasureUnit.MILLILITER
        DefaultUnit.LITRE -> return MeasureUnit.LITER
        DefaultUnit.CUBIC_CENTIMETRE -> return MeasureUnit.CUBIC_CENTIMETER
        DefaultUnit.METRE -> return MeasureUnit.METER
        DefaultUnit.KILOMETRE -> return MeasureUnit.KILOMETER
        DefaultUnit.CENTIMETRE -> return MeasureUnit.CENTIMETER
        DefaultUnit.MILLIMETRE -> return MeasureUnit.MILLIMETER
        DefaultUnit.INCH -> return MeasureUnit.INCH
        DefaultUnit.FOOT -> return MeasureUnit.FOOT
        DefaultUnit.YARD -> return MeasureUnit.YARD
        DefaultUnit.MILE -> return MeasureUnit.MILE
        DefaultUnit.SQUARE_METRE -> return MeasureUnit.SQUARE_METER
        DefaultUnit.SQUARE_CENTIMETRE -> return MeasureUnit.SQUARE_CENTIMETER
        DefaultUnit.SQUARE_KILOMETRE -> return MeasureUnit.SQUARE_KILOMETER
        DefaultUnit.HECTARE -> return MeasureUnit.HECTARE
        DefaultUnit.ACRE -> return MeasureUnit.ACRE
        DefaultUnit.SQUARE_FOOT -> return MeasureUnit.SQUARE_FOOT
        DefaultUnit.SQUARE_INCH -> return MeasureUnit.SQUARE_INCH
        DefaultUnit.SQUARE_YARD -> return MeasureUnit.SQUARE_YARD
        DefaultUnit.SQUARE_MILE -> return MeasureUnit.SQUARE_MILE
        DefaultUnit.US_CUP -> return MeasureUnit.CUP
        DefaultUnit.US_GALLON -> return MeasureUnit.GALLON
        DefaultUnit.US_PINT -> return MeasureUnit.PINT
        DefaultUnit.QUART, DefaultUnit.US_QUART -> return MeasureUnit.QUART
        DefaultUnit.FLUID_OUNCE, DefaultUnit.US_FLUID_OUNCE -> return MeasureUnit.FLUID_OUNCE
        DefaultUnit.TEASPOON, DefaultUnit.US_TEASPOON -> return MeasureUnit.TEASPOON
        DefaultUnit.TABLESPOON, DefaultUnit.US_TABLESPOON -> return MeasureUnit.TABLESPOON
    }

    if (VERSION.SDK_INT >= 26) {
        when (this) {
            DefaultUnit.CUP -> return MeasureUnit.CUP_METRIC
            DefaultUnit.PINT -> return MeasureUnit.PINT_METRIC
        }
    } else {
        when (this) {
            DefaultUnit.CUP -> return MeasureUnit.CUP
            DefaultUnit.PINT -> return MeasureUnit.PINT
        }
    }

    if (VERSION.SDK_INT >= 28) {
        when (this) {
            DefaultUnit.GALLON -> return MeasureUnit.GALLON_IMPERIAL
        }
    } else {
        when (this) {
            DefaultUnit.GALLON -> return MeasureUnit.GALLON
        }
    }

    return null
}