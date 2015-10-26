package com.unitpricecalculator.comparisons;

import com.google.common.collect.ImmutableList;

import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;

public final class SavedComparison {

    private final String name;
    private final UnitType unitType;
    private final ImmutableList<SavedUnitEntryRow> savedUnitEntryRows;
    private final String finalQuantity;
    private final Unit finalUnit;

    public SavedComparison(String name, UnitType unitType, ImmutableList<SavedUnitEntryRow> savedUnitEntryRows,
                           String finalQuantity, Unit finalUnit) {
        this.name = name;
        this.unitType = unitType;
        this.savedUnitEntryRows = savedUnitEntryRows;
        this.finalQuantity = finalQuantity;
        this.finalUnit = finalUnit;
    }

    public ImmutableList<SavedUnitEntryRow> getSavedUnitEntryRows() {
        return savedUnitEntryRows;
    }

    public String getFinalQuantity() {
        return finalQuantity;
    }

    public Unit getFinalUnit() {
        return finalUnit;
    }

    public String getName() {
        return name;
    }

    public UnitType getUnitType() {
        return unitType;
    }
}
