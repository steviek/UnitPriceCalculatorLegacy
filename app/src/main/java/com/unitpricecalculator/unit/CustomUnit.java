package com.unitpricecalculator.unit;

import com.google.common.base.Throwables;

import android.os.Parcel;

import com.unitpricecalculator.util.prefs.Prefs;
import com.unitpricecalculator.util.prefs.StringDeserializer;
import com.unitpricecalculator.util.prefs.StringSerializer;

import org.json.JSONException;
import org.json.JSONObject;

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

    public static CustomUnit fromKey(String key) {
        return Prefs.getStringSerializable(CustomUnit.class, key);
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
            return fromKey(source.readString());
        }

        @Override
        public CustomUnit[] newArray(int size) {
            return new CustomUnit[size];
        }
    };

    public static final StringSerializer<CustomUnit> STRING_SERIALIZER = new StringSerializer<CustomUnit>() {
        @Override
        public String serialize(CustomUnit object) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("key", object.key);
                obj.put("symbol", object.symbol);
                obj.put("system", object.system.name());
                obj.put("unitType", object.unitType.name());
                obj.put("factor", object.factor);
                return obj.toString();
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
    };

    public static final StringDeserializer<CustomUnit> STRING_DESERIALIZER = new StringDeserializer<CustomUnit>() {
        @Override
        public CustomUnit deserialize(String s) {
            try {
                JSONObject obj = new JSONObject(s);
                String key = obj.getString("key");
                String symbol = obj.getString("symbol");
                String system = obj.getString("system");
                String unitType = obj.getString("unitType");
                double factor = obj.getDouble("factor");
                return new CustomUnit(key, symbol, System.valueOf(system), UnitType.valueOf(unitType), factor);
            } catch (JSONException e) {
                throw Throwables.propagate(e);
            }
        }
    };
}
