package com.unitpricecalculator.unit;

import android.content.res.Resources;
import android.icu.util.MeasureUnit;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unitpricecalculator.R;

public enum DefaultUnit implements Unit {
  GRAM(R.string.gram_symbol, 1, System.METRIC, UnitType.WEIGHT, MeasureUnit.GRAM, 100),
  KILOGRAM(R.string.kilogram_symbol, 1000, System.METRIC, UnitType.WEIGHT, MeasureUnit.KILOGRAM),
  MILLIGRAM(R.string.milligram_symbol, 0.001, System.METRIC, UnitType.WEIGHT, MeasureUnit.MILLIGRAM, 100),
  OUNCE(R.string.ounce_symbol, 28.3495, System.IMPERIAL, UnitType.WEIGHT, MeasureUnit.OUNCE),
  POUND(R.string.pound_symbol, 453.592, System.IMPERIAL, UnitType.WEIGHT, MeasureUnit.POUND),
  STONE(R.string.stone_symbol, 6350.293, System.IMPERIAL, UnitType.WEIGHT, MeasureUnit.STONE),
  MILLILITRE(R.string.millilitre_symbol, 0.001, System.METRIC, UnitType.VOLUME, MeasureUnit.MILLILITER, 100),
  LITRE(R.string.litre_symbol, 1, System.METRIC, UnitType.VOLUME, MeasureUnit.LITER),
  CUBIC_CENTIMETRE(R.string.cubic_centimetre_symbol, 0.001, System.METRIC, UnitType.VOLUME, MeasureUnit.CUBIC_CENTIMETER, 100),
  GALLON(R.string.gallon_symbol, 4.54609, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.GALLON),
  QUART(R.string.quart_symbol, 1.13652, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.QUART),
  PINT(R.string.pint_symbol, 0.568261, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.PINT),
  CUP(R.string.cup_symbol, 0.284131, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.CUP),
  FLUID_OUNCE(R.string.fluid_ounce_symbol, 0.0284131, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.FLUID_OUNCE),
  TABLESPOON(R.string.tablespoon_symbol, 0.0177582, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.TABLESPOON),
  TEASPOON(R.string.teaspoon_symbol, 0.00591939, System.IMPERIAL_UK, UnitType.VOLUME, MeasureUnit.TEASPOON),
  US_GALLON(R.string.us_gallon_symbol, 3.78541, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.GALLON),
  US_QUART(R.string.us_quart_symbol, 0.946353, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.QUART),
  US_PINT(R.string.us_pint_symbol, 0.473176, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.PINT),
  US_CUP(R.string.us_cup_symbol, 0.236588, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.CUP),
  US_FLUID_OUNCE(R.string.us_fluid_ounce_symbol, 0.0295735, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.FLUID_OUNCE),
  US_TABLESPOON(R.string.us_tablespoon_symbol, 0.0147868, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.TABLESPOON),
  US_TEASPOON(R.string.us_teaspoon_symbol, 0.00492892, System.IMPERIAL_US, UnitType.VOLUME, MeasureUnit.TEASPOON),
  METRE(R.string.metre_symbol, 1, System.METRIC, UnitType.LENGTH, MeasureUnit.METER),
  KILOMETRE(R.string.kilometre_symbol, 1000, System.METRIC, UnitType.LENGTH, MeasureUnit.KILOMETER),
  CENTIMETRE(R.string.centimetre_symbol, 0.01, System.METRIC, UnitType.LENGTH, MeasureUnit.CENTIMETER, 10),
  MILLIMETRE(R.string.millimetre_symbol, 0.001, System.METRIC, UnitType.LENGTH, MeasureUnit.MILLIMETER, 100),
  INCH(R.string.inch_symbol, 0.0254, System.IMPERIAL, UnitType.LENGTH, MeasureUnit.INCH),
  FOOT(R.string.foot_symbol, 0.3048, System.IMPERIAL, UnitType.LENGTH, MeasureUnit.FOOT),
  YARD(R.string.yard_symbol, 0.9144, System.IMPERIAL, UnitType.LENGTH, MeasureUnit.YARD),
  MILE(R.string.mile_symbol, 1609.34, System.IMPERIAL, UnitType.LENGTH, MeasureUnit.MILE),
  SQUARE_METRE(R.string.square_metre_symbol, 1, System.METRIC, UnitType.AREA, MeasureUnit.SQUARE_METER),
  SQUARE_CENTIMETRE(R.string.square_centimetre_symbol, 0.0001, System.METRIC, UnitType.AREA, MeasureUnit.SQUARE_CENTIMETER),
  SQUARE_MILLIMETRE(R.string.square_millimetre_symbol, 0.000001, System.METRIC, UnitType.AREA),
  SQUARE_KILOMETRE(R.string.square_kilometre_symbol, 1000000, System.METRIC, UnitType.AREA, MeasureUnit.SQUARE_KILOMETER),
  ARE(R.string.are_symbol, 100, System.METRIC, UnitType.AREA, null),
  HECTARE(R.string.hectare_symbol, 10000, System.METRIC, UnitType.AREA, MeasureUnit.HECTARE),
  ACRE(R.string.acre_symbol, 4046.856, System.IMPERIAL, UnitType.AREA, MeasureUnit.ACRE),
  SQUARE_FOOT(R.string.square_foot_symbol, 0.092903, System.IMPERIAL, UnitType.AREA, MeasureUnit.SQUARE_FOOT),
  SQUARE_INCH(R.string.square_inch_symbol, 0.00064516, System.IMPERIAL, UnitType.AREA, MeasureUnit.SQUARE_INCH),
  SQUARE_YARD(R.string.square_yard_symbol, 0.836127, System.IMPERIAL, UnitType.AREA, MeasureUnit.SQUARE_YARD),
  SQUARE_MILE(R.string.square_mile_symbol, 2589990, System.IMPERIAL, UnitType.AREA, MeasureUnit.SQUARE_MILE),
  DAY(R.string.day_symbol, 1, System.NEUTRAL, UnitType.TIME, MeasureUnit.DAY),
  WEEK(R.string.week_symbol, 7, System.NEUTRAL, UnitType.TIME, MeasureUnit.WEEK),
  MONTH(R.string.month_symbol, 365.25 / 12, System.NEUTRAL, UnitType.TIME, MeasureUnit.MONTH),
  YEAR(R.string.year_symbol, 365.25, System.NEUTRAL, UnitType.TIME, MeasureUnit.YEAR),
  SECOND(R.string.second_symbol, 1.0 / (24 * 60 * 60), System.NEUTRAL, UnitType.TIME, MeasureUnit.SECOND),
  MINUTE(R.string.minute_symbol, 1.0 / (24 * 60), System.NEUTRAL, UnitType.TIME, MeasureUnit.MINUTE),
  HOUR(R.string.hour_symbol, 1.0 / 24, System.NEUTRAL, UnitType.TIME, MeasureUnit.HOUR),
  UNIT(R.string.unit_symbol, 1, System.METRIC, UnitType.QUANTITY),
  DOZEN(R.string.dozen_symbol, 12, System.METRIC, UnitType.QUANTITY);
  private final int symbol;
  private final double factor;
  private final System system;
  private final UnitType unitType;
  @Nullable
  private final MeasureUnit measureUnit;
  private final int defaultQuantity;

  DefaultUnit(int symbol, double factor, System system, UnitType unitType) {
    this(symbol, factor, system, unitType, null);
  }

  DefaultUnit(int symbol, double factor, System system, UnitType unitType, @Nullable MeasureUnit measureUnit) {
    this(symbol, factor, system, unitType, measureUnit, 1);
  }

  DefaultUnit(int symbol, double factor, System system, UnitType unitType, @Nullable MeasureUnit measureUnit, int defaultQuantity) {
    this.symbol = symbol;
    this.factor = factor;
    this.system = system;
    this.unitType = unitType;
    this.measureUnit = measureUnit;
    this.defaultQuantity = defaultQuantity;
  }

  @Override
  public String getSymbol(Resources resources) {
    return resources.getString(symbol);
  }

  @Override
  public double getFactor() {
    return factor;
  }

  @Override
  public System getSystem() {
    return system;
  }

  @NonNull
  @Override
  public UnitType getUnitType() {
    return unitType;
  }

  @Override
  public int getDefaultQuantity() {
    return defaultQuantity;
  }

  @Nullable
  public MeasureUnit getMeasureUnit() {
    return measureUnit;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name());
  }

  public static final Parcelable.Creator<DefaultUnit> CREATOR =
      new Parcelable.Creator<DefaultUnit>() {
        @Override
        public DefaultUnit createFromParcel(Parcel source) {
          return valueOf(source.readString());
        }

        @Override
        public DefaultUnit[] newArray(int size) {
          return new DefaultUnit[size];
        }
      };
}
