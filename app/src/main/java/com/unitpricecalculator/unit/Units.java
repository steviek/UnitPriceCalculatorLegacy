package com.unitpricecalculator.unit;

import com.google.common.collect.ImmutableList;

import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of utility functions for {@link Unit}.
 */
public final class Units {

    private Units() {}

    private static Map<UnitType, ImmutableList<Unit>> unitMap = new HashMap<>();

    public static ImmutableList<Unit> getUnitsForType(UnitType unitType) {
        if (unitMap.get(unitType) == null) {
            ImmutableList.Builder<Unit> list = ImmutableList.builder();
            for (Unit unit : DefaultUnit.values()) {
                if (unit.getUnitType() == unitType) {
                    list.add(unit);
                }
            }
            unitMap.put(unitType, list.build());
        }
        return unitMap.get(unitType);
    }

    public static UnitType getCurrentUnitType() {
        return UnitType.valueOf(Prefs.getString(Keys.UNIT_TYPE, UnitType.WEIGHT.name()));
    }

    public static void setCurrentUnitType(UnitType unitType) {
        Prefs.putString(Keys.UNIT_TYPE, unitType.name());
        MyApplication.getInstance().getBus().post(new UnitTypeChangedEvent(unitType));
    }
}
