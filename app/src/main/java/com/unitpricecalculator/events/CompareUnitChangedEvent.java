package com.unitpricecalculator.events;

import com.unitpricecalculator.unit.Unit;

public final class CompareUnitChangedEvent {

    private final String size;
    private final Unit unit;

    public CompareUnitChangedEvent(String size, Unit unit) {
        this.size = size;
        this.unit = unit;
    }

    public String getSize() {
        return size;
    }

    public Unit getUnit() {
        return unit;
    }
}
