package com.unitpricecalculator.comparisons;

import com.google.common.collect.ImmutableList;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;

public final class SavedComparison implements Comparable<SavedComparison> {

    private final String name;
    private final UnitType unitType;
    private final ImmutableList<SavedUnitEntryRow> savedUnitEntryRows;
    private final String finalQuantity;
    private final Unit finalUnit;

    @JsonCreator
    public SavedComparison(
            @JsonProperty("name") String name,
            @JsonProperty("unitType") UnitType unitType,
            @JsonProperty("entryRows") ImmutableList<SavedUnitEntryRow> savedUnitEntryRows,
            @JsonProperty("finalQuantity") String finalQuantity,
            @JsonProperty("finalUnit") Unit finalUnit) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedComparison that = (SavedComparison) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (unitType != that.unitType) return false;
        if (savedUnitEntryRows != null ? !savedUnitEntryRows.equals(that.savedUnitEntryRows) : that.savedUnitEntryRows != null)
            return false;
        if (finalQuantity != null ? !finalQuantity.equals(that.finalQuantity) : that.finalQuantity != null)
            return false;
        return !(finalUnit != null ? !finalUnit.equals(that.finalUnit) : that.finalUnit != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (unitType != null ? unitType.hashCode() : 0);
        result = 31 * result + (savedUnitEntryRows != null ? savedUnitEntryRows.hashCode() : 0);
        result = 31 * result + (finalQuantity != null ? finalQuantity.hashCode() : 0);
        result = 31 * result + (finalUnit != null ? finalUnit.hashCode() : 0);
        return result;
    }

    /**
     * Generates a copy of this comparison with the provided name.  The original object is unaffected.
     */
    public SavedComparison rename(String newName) {
        return new SavedComparison(newName, unitType, savedUnitEntryRows, finalQuantity, finalUnit);
    }

    @Override
    public int compareTo(@NonNull SavedComparison another) {
        return name.compareTo(another.name);
    }
}
