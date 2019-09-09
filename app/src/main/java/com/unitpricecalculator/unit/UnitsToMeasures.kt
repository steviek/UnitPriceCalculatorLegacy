package com.unitpricecalculator.unit

import android.annotation.TargetApi
import android.icu.util.MeasureUnit
import android.os.Build

@TargetApi(Build.VERSION_CODES.N)
fun getMeasureUnit(unit: Unit): MeasureUnit? {
    if (unit !is DefaultUnit) {
        return null
    }

    when (unit) {
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
    }

    return null
}