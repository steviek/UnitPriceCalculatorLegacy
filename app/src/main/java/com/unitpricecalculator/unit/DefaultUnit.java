package com.unitpricecalculator.unit;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import com.unitpricecalculator.R;

public enum DefaultUnit implements Unit {
    GRAM(R.string.gram_symbol, 1, System.METRIC, UnitType.WEIGHT, 100),
    KILOGRAM(R.string.kilogram_symbol, 1000, System.METRIC, UnitType.WEIGHT),
    MILLIGRAM(R.string.milligram_symbol, 0.001, System.METRIC, UnitType.WEIGHT, 100),
    OUNCE(R.string.ounce_symbol, 28.3495, System.IMPERIAL, UnitType.WEIGHT),
    POUND(R.string.pound_symbol, 453.592, System.IMPERIAL, UnitType.WEIGHT),
    MILLILITRE(R.string.millilitre_symbol, 0.001, System.METRIC, UnitType.VOLUME, 100),
    LITRE(R.string.litre_symbol, 1, System.METRIC, UnitType.VOLUME),
    CUBIC_CENTIMETRE(R.string.cubic_centimetre_symbol, 0.001, System.METRIC, UnitType.VOLUME, 100),
    GALLON(R.string.gallon_symbol, 4.54609, System.IMPERIAL_UK, UnitType.VOLUME),
    QUART(R.string.quart_symbol, 1.13652, System.IMPERIAL_UK, UnitType.VOLUME),
    PINT(R.string.pint_symbol, 0.568261, System.IMPERIAL_UK, UnitType.VOLUME),
    CUP(R.string.cup_symbol, 0.284, System.IMPERIAL_UK, UnitType.VOLUME),
    FLUID_OUNCE(R.string.fluid_ounce_symbol, 0.0284131, System.IMPERIAL_UK, UnitType.VOLUME),
    TABLESPOON(R.string.tablespoon_symbol, 0.0177582, System.IMPERIAL_UK, UnitType.VOLUME),
    TEASPOON(R.string.teaspoon_symbol, 0.00591939, System.IMPERIAL_UK, UnitType.VOLUME),
    US_GALLON(R.string.us_gallon_symbol, 3.78541, System.IMPERIAL_US, UnitType.VOLUME),
    US_QUART(R.string.us_quart_symbol, 0.946353, System.IMPERIAL_US, UnitType.VOLUME),
    US_PINT(R.string.us_pint_symbol, 0.473176, System.IMPERIAL_US, UnitType.VOLUME),
    US_CUP(R.string.us_cup_symbol, 0.236588, System.IMPERIAL_US, UnitType.VOLUME),
    US_FLUID_OUNCE(R.string.us_fluid_ounce_symbol, 0.0295735, System.IMPERIAL_US, UnitType.VOLUME),
    US_TABLESPOON(R.string.us_tablespoon_symbol, 0.0147868, System.IMPERIAL_US, UnitType.VOLUME),
    US_TEASPOON(R.string.us_teaspoon_symbol, 0.00492892, System.IMPERIAL_US, UnitType.VOLUME),
    METRE(R.string.metre_symbol, 1, System.METRIC, UnitType.LENGTH),
    CENTIMETRE(R.string.centimetre_symbol, 0.01, System.METRIC, UnitType.LENGTH, 10),
    MILLIMETRE(R.string.millimetre_symbol, 0.001, System.METRIC, UnitType.LENGTH, 100),
    INCH(R.string.inch_symbol, 0.0254, System.IMPERIAL, UnitType.LENGTH),
    FOOT(R.string.foot_symbol, 0.3048, System.IMPERIAL, UnitType.LENGTH),
    YARD(R.string.yard_symbol, 0.9144, System.IMPERIAL, UnitType.LENGTH),
    UNIT(R.string.unit_symbol, 1, System.METRIC, UnitType.QUANTITY),
    DOZEN(R.string.dozen_symbol, 12, System.METRIC, UnitType.QUANTITY);

    private final int symbol;
    private final double factor;
    private final System system;
    private final UnitType unitType;
    private final int defaultQuantity;

    DefaultUnit(int symbol, double factor, System system, UnitType unitType) {
        this(symbol, factor, system, unitType, 1);
    }

    DefaultUnit(int symbol, double factor, System system, UnitType unitType, int defaultQuantity) {
        this.symbol = symbol;
        this.factor = factor;
        this.system = system;
        this.unitType = unitType;
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

    @Override
    public UnitType getUnitType() {
        return unitType;
    }

    @Override
    public int getDefaultQuantity() {
        return defaultQuantity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }

    public static final Parcelable.Creator<DefaultUnit> CREATOR = new Parcelable.Creator<DefaultUnit>() {
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
