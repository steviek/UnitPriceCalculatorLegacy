package com.unitpricecalculator.unit;

import android.os.Parcel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CustomUnit implements Unit {

    private final String key;

    private final String symbol;

    private final System system;

    private final UnitType unitType;

    private final double factor;

    @JsonCreator
    public CustomUnit(
            @JsonProperty("key") String key,
            @JsonProperty("symbol") String symbol,
            @JsonProperty("system") System system,
            @JsonProperty("unitType") UnitType unitType,
            @JsonProperty("factor") double factor) {
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
        dest.writeString(symbol);
        dest.writeString(system.name());
        dest.writeString(unitType.name());
        dest.writeDouble(factor);
    }

    public static final Creator<CustomUnit> CREATOR = new Creator<CustomUnit>() {
        @Override
        public CustomUnit createFromParcel(Parcel source) {
            String key = source.readString();
            String symbol = source.readString();
            System system = System.valueOf(source.readString());
            UnitType unitType = UnitType.valueOf(source.readString());
            double factor = source.readDouble();
            return new CustomUnit(key, symbol, system, unitType, factor);
        }

        @Override
        public CustomUnit[] newArray(int size) {
            return new CustomUnit[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomUnit that = (CustomUnit) o;

        if (Double.compare(that.factor, factor) != 0) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
        if (system != that.system) return false;
        return unitType == that.unitType;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = key != null ? key.hashCode() : 0;
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        result = 31 * result + (system != null ? system.hashCode() : 0);
        result = 31 * result + (unitType != null ? unitType.hashCode() : 0);
        temp = Double.doubleToLongBits(factor);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
