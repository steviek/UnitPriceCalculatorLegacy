package com.unitpricecalculator.unit;

import com.google.common.collect.ImmutableList;
import com.squareup.otto.Bus;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.Reusable;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;

/**
 * Collection of utility functions for {@link Unit}.
 */
@Reusable
public final class Units {

    private final Prefs prefs;
    private final Bus bus;

    @Inject
    Units(Prefs prefs, Bus bus) {
        this.prefs = prefs;
        this.bus = bus;
    }

    private static Map<UnitType, ImmutableList<Unit>> unitMap = new HashMap<>();

    public ImmutableList<Unit> getUnitsForType(UnitType unitType) {
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

    public UnitType getCurrentUnitType() {
        return UnitType.valueOf(prefs.getString(Keys.UNIT_TYPE, UnitType.WEIGHT.name()));
    }

    public void setCurrentUnitType(UnitType unitType) {
        prefs.putString(Keys.UNIT_TYPE, unitType.name());
        bus.post(new UnitTypeChangedEvent(unitType));
    }

    public Currency getCurrency() {
        if (prefs.getString("currency") != null) {
            return Currency.getInstance(prefs.getString("currency"));
        }
        return Currency.getInstance(Locale.getDefault());
    }

    public void setCurrency(Currency currency) {
        prefs.putString("currency", currency.getCurrencyCode());
    }
}
