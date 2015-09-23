package com.unitpricecalculator.events;

import com.unitpricecalculator.unit.UnitType;

public final class UnitTypeChangedEvent {

  private final UnitType unitType;

  public UnitTypeChangedEvent(UnitType unitType) {
    this.unitType = unitType;
  }

  public UnitType getUnitType() {
    return unitType;
  }
}
