package com.unitpricecalculator.unit;

import com.unitpricecalculator.R;

public enum Unit {
  GRAM(R.plurals.gram, R.string.gram_symbol, 1, System.METRIC, UnitType.WEIGHT),
  KILOGRAM(R.plurals.kilogram, R.string.kilogram_symbol, 1000, System.METRIC, UnitType.WEIGHT),
  MILLIGRAM(R.plurals.milligram, R.string.milligram_symbol, 0.001, System.METRIC, UnitType.WEIGHT),
  OUNCE(R.plurals.ounce, R.string.ounce_symbol, 28.3495, System.IMPERIAL, UnitType.WEIGHT),
  POUND(R.plurals.pound, R.string.pound_symbol, 453.592, System.IMPERIAL, UnitType.WEIGHT),
  MILLILITRE(R.plurals.millilitre, R.string.millilitre_symbol, 0.001, System.METRIC, UnitType.VOLUME),
  LITRE(R.plurals.litre, R.string.litre_symbol, 1, System.METRIC, UnitType.VOLUME),
  CUBIC_CENTIMETRE(R.plurals.cubic_centimetre, R.string.cubic_centimetre_symbol, 0.001, System.METRIC, UnitType.VOLUME),
  GALLON(R.plurals.gallon, R.string.gallon_symbol, 4.54609, System.IMPERIAL_UK, UnitType.VOLUME),
  QUART(R.plurals.quart, R.string.quart_symbol, 1.13652, System.IMPERIAL_UK, UnitType.VOLUME),
  PINT(R.plurals.pint, R.string.pint_symbol, 0.568261, System.IMPERIAL_UK, UnitType.VOLUME),
  CUP(R.plurals.cup, R.string.cup_symbol, 0.284, System.IMPERIAL_UK, UnitType.VOLUME),
  FLUID_OUNCE(R.plurals.fluid_ounce, R.string.fluid_ounce_symbol, 0.0284131, System.IMPERIAL_UK, UnitType.VOLUME),
  TABLESPOON(R.plurals.tablespoon, R.string.tablespoon_symbol, 0.0177582, System.IMPERIAL_UK, UnitType.VOLUME),
  TEASPOON(R.plurals.teaspoon, R.string.teaspoon_symbol, 0.00591939, System.IMPERIAL_UK, UnitType.VOLUME),
  US_GALLON(R.plurals.us_gallon, R.string.us_gallon_symbol, 3.78541, System.IMPERIAL_US, UnitType.VOLUME),
  US_QUART(R.plurals.us_quart, R.string.us_quart_symbol, 0.946353, System.IMPERIAL_US, UnitType.VOLUME),
  US_PINT(R.plurals.us_pint, R.string.us_pint_symbol, 0.473176, System.IMPERIAL_US, UnitType.VOLUME),
  US_CUP(R.plurals.us_cup, R.string.us_cup_symbol, 0.236588, System.IMPERIAL_US, UnitType.VOLUME),
  US_FLUID_OUNCE(R.plurals.us_fluid_ounce, R.string.us_fluid_ounce_symbol, 0.0295735, System.IMPERIAL_US, UnitType.VOLUME),
  US_TABLESPOON(R.plurals.us_tablespoon, R.string.us_tablespoon_symbol, 0.0147868, System.IMPERIAL_US, UnitType.VOLUME),
  US_TEASPOON(R.plurals.us_teaspoon, R.string.us_teaspoon_symbol, 0.00492892, System.IMPERIAL_US, UnitType.VOLUME),
  METRE(R.plurals.metre, R.string.metre_symbol, 1, System.METRIC, UnitType.LENGTH),
  CENTIMETRE(R.plurals.centimetre, R.string.centimetre_symbol, 0.01, System.METRIC, UnitType.LENGTH),
  MILLIMETRE(R.plurals.millimetre, R.string.millimetre_symbol, 0.001, System.METRIC, UnitType.LENGTH),
  INCH(R.plurals.inch, R.string.inch_symbol, 0.0254, System.IMPERIAL, UnitType.LENGTH),
  FOOT(R.plurals.foot, R.string.foot_symbol, 0.3048, System.IMPERIAL, UnitType.LENGTH),
  YARD(R.plurals.yard, R.string.yard_symbol, 0.9144, System.IMPERIAL, UnitType.LENGTH),
  UNIT(R.plurals.unit, R.string.unit_symbol, 1, System.METRIC, UnitType.QUANTITY);

  private final int plurals;
  private final int symbol;
  private final double factor;
  private final System system;
  private final UnitType unitType;

  Unit(int plurals, int symbol, double factor, System system, UnitType unitType) {
    this.plurals = plurals;
    this.symbol = symbol;
    this.factor = factor;
    this.system = system;
    this.unitType = unitType;
  }

  public int getPlurals() {
    return plurals;
  }

  public int getSymbol() {
    return symbol;
  }

  public double getFactor() {
    return factor;
  }

  public System getSystem() {
    return system;
  }

  public UnitType getUnitType() {
    return unitType;
  }
}
