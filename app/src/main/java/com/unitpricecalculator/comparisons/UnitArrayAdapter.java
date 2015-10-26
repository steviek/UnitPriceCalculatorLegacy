package com.unitpricecalculator.comparisons;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;

import java.util.HashSet;
import java.util.Set;

final class UnitArrayAdapter extends ArrayAdapter<String> {

    private final ImmutableList<Unit> units;

    static UnitArrayAdapter of(Context context, UnitType unitType) {
        return of(context, unitType, null);
    }

    static UnitArrayAdapter of(Context context, Unit unit) {
        return of(context, unit.getUnitType(), unit);
    }

    private static UnitArrayAdapter of(Context context, UnitType unitType, Unit selected) {
        ImmutableList.Builder<Unit> units = ImmutableList.builder();
        ImmutableList.Builder<String> symbols = ImmutableList.builder();

        Set<Unit> includedUnits = new HashSet<>();

        if (selected != null) {
            includedUnits.add(selected);
            units.add(selected);
            symbols.add(selected.getSymbol());
        }

        for (System system : System.getPreferredOrder()) {
            for (Unit unit : Units.getUnitsForType(unitType)) {
                if (!includedUnits.contains(unit) && unit.getSystem().is(system) &&
                        (selected == null || unit != selected)) {
                    includedUnits.add(unit);
                    units.add(unit);
                    symbols.add(unit.getSymbol());
                }
            }
        }
        return new UnitArrayAdapter(context, symbols.build(), units.build());
    }

    private UnitArrayAdapter(Context context, ImmutableList<String> unitSymbols, ImmutableList<Unit> units) {
        super(context, android.R.layout.simple_dropdown_item_1line, unitSymbols);
        this.units = Preconditions.checkNotNull(units);
    }

    Unit getUnit(int position) {
        return units.get(position);
    }
}
