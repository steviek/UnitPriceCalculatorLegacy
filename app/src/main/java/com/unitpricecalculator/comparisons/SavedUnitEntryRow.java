package com.unitpricecalculator.comparisons;

import com.google.common.base.Preconditions;

import com.unitpricecalculator.unit.Unit;

public class SavedUnitEntryRow {

    private final String cost;
    private final String quantity;
    private final String size;
    private final Unit unit;

    public SavedUnitEntryRow(String cost, String quantity, String size, Unit unit) {
        this.cost = Preconditions.checkNotNull(cost);
        this.quantity = Preconditions.checkNotNull(quantity);
        this.size = Preconditions.checkNotNull(size);
        this.unit = Preconditions.checkNotNull(unit);
    }

    public String getCost() {
        return cost;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSize() {
        return size;
    }

    public Unit getUnit() {
        return unit;
    }
}
