package com.unitpricecalculator.unit;

import android.os.Parcel;

import com.unitpricecalculator.util.prefs.Prefs;

public final class CustomUnit implements Unit {

    private final String key;

    private final String symbol;

    private final System system;

    private final UnitType unitType;

    private final double factor;

    public CustomUnit(String key, String symbol, System system, UnitType unitType, double factor) {
        this.key = key;
        this.symbol = symbol;
        this.system = system;
        this.unitType = unitType;
        this.factor = factor;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String getSymbol() {
        return symbol;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
    }

    public static final Creator<CustomUnit> CREATOR = new Creator<CustomUnit>() {
        @Override
        public CustomUnit createFromParcel(Parcel source) {
            return Prefs.getObject(CustomUnit.class, source.readString());
        }

        @Override
        public CustomUnit[] newArray(int size) {
            return new CustomUnit[size];
        }
    };
}
