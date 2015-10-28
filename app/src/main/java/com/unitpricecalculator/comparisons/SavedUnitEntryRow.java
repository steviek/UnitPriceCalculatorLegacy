package com.unitpricecalculator.comparisons;

import com.google.common.base.Preconditions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unitpricecalculator.unit.Unit;

public class SavedUnitEntryRow {

    private final String cost;
    private final String quantity;
    private final String size;
    private final Unit unit;

    @JsonCreator
    public SavedUnitEntryRow(
            @JsonProperty("cost") String cost,
            @JsonProperty("quantity") String quantity,
            @JsonProperty("size") String size,
            @JsonProperty("unit") Unit unit) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SavedUnitEntryRow that = (SavedUnitEntryRow) o;

        if (cost != null ? !cost.equals(that.cost) : that.cost != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (size != null ? !size.equals(that.size) : that.size != null) return false;
        return !(unit != null ? !unit.equals(that.unit) : that.unit != null);

    }

    @Override
    public int hashCode() {
        int result = cost != null ? cost.hashCode() : 0;
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
