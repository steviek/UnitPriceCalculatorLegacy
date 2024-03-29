package com.unitpricecalculator.comparisons;

import android.content.Context;
import android.util.Pair;
import android.widget.ArrayAdapter;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.R;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Systems;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;
import java.util.HashSet;
import java.util.Set;

public final class UnitArrayAdapter extends ArrayAdapter<String> {

  private final ImmutableList<DefaultUnit> units;

  public UnitArrayAdapter(
      Context context,
      Systems systems,
      Units units,
      UnitType unitType) {
    this(context, getSymbolsAndUnits(context, systems, units, unitType, /* selected= */ null));
  }

  public UnitArrayAdapter(
      Context context,
      Systems systems,
      Units units,
      DefaultUnit selected) {
    this(context, getSymbolsAndUnits(context, systems, units, selected.getUnitType(), selected));
  }

  static Pair<ImmutableList<String>, ImmutableList<DefaultUnit>> getSymbolsAndUnits(
      Context context, Systems systems, Units units, UnitType unitType, DefaultUnit selected) {
    ImmutableList.Builder<DefaultUnit> unitslist = ImmutableList.builder();
    ImmutableList.Builder<String> symbols = ImmutableList.builder();

    Set<DefaultUnit> includedUnits = new HashSet<>();

    if (selected != null) {
      includedUnits.add(selected);
      unitslist.add(selected);
      symbols.add(selected.getSymbol(context.getResources()));
    }

    Set<System> includedSystems = systems.getIncludedSystems();
    for (System system : systems.getPreferredOrder()) {
      if (!includedSystems.contains(system)) {
        continue;
      }
      for (DefaultUnit unit : units.getUnitsForType(unitType)) {
        if (!includedUnits.contains(unit) && unit.getSystem().is(system) &&
            (selected == null || unit != selected)
            && Systems.includes(includedSystems, unit.getSystem())) {
          includedUnits.add(unit);
          unitslist.add(unit);
          symbols.add(unit.getSymbol(context.getResources()));
        }
      }
    }
    return Pair.create(symbols.build(), unitslist.build());
  }

  private UnitArrayAdapter(
      Context context, Pair<ImmutableList<String>, ImmutableList<DefaultUnit>> symbolsAndUnits) {
    super(context, R.layout.unit_type_spinner_dropdown_item, symbolsAndUnits.first);
    this.units = Preconditions.checkNotNull(symbolsAndUnits.second);
  }

  DefaultUnit getUnit(int position) {
    return units.get(position);
  }
}
