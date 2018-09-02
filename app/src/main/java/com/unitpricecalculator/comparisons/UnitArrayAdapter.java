package com.unitpricecalculator.comparisons;

import android.content.Context;
import android.util.Pair;
import android.widget.ArrayAdapter;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.inject.ApplicationContext;
import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Systems;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;

import java.util.HashSet;
import java.util.Set;

final class UnitArrayAdapter extends ArrayAdapter<String> {

    private final ImmutableList<Unit> units;

    @AutoFactory
    UnitArrayAdapter(
            @Provided @ApplicationContext Context context,
            @Provided Systems systems,
            @Provided Units units,
            UnitType unitType) {
        this(context, getSymbolsAndUnits(systems, units, unitType, /* selected= */ null));
    }

    @AutoFactory
    UnitArrayAdapter(
            @Provided @ApplicationContext Context context,
            @Provided Systems systems,
            @Provided Units units,
            Unit selected) {
        this(context, getSymbolsAndUnits(systems, units, selected.getUnitType(), selected));
    }

    private static Pair<ImmutableList<String>, ImmutableList<Unit>> getSymbolsAndUnits(
            Systems systems, Units units, UnitType unitType, Unit selected) {
        ImmutableList.Builder<Unit> unitslist = ImmutableList.builder();
        ImmutableList.Builder<String> symbols = ImmutableList.builder();

        Set<Unit> includedUnits = new HashSet<>();

        if (selected != null) {
            includedUnits.add(selected);
            unitslist.add(selected);
            symbols.add(selected.getSymbol());
        }

        for (System system : systems.getPreferredOrder()) {
            for (Unit unit : units.getUnitsForType(unitType)) {
                if (!includedUnits.contains(unit) && unit.getSystem().is(system) &&
                        (selected == null || unit != selected)) {
                    includedUnits.add(unit);
                    unitslist.add(unit);
                    symbols.add(unit.getSymbol());
                }
            }
        }
        return Pair.create(symbols.build(), unitslist.build());
    }

    private UnitArrayAdapter(
            Context context, Pair<ImmutableList<String>, ImmutableList<Unit>> symbolsAndUnits) {
        super(context, android.R.layout.simple_dropdown_item_1line, symbolsAndUnits.first);
        this.units = Preconditions.checkNotNull(symbolsAndUnits.second);
    }

    Unit getUnit(int position) {
        return units.get(position);
    }
}
