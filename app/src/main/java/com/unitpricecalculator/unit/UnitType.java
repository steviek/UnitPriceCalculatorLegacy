package com.unitpricecalculator.unit;

import android.content.res.Resources;
import com.unitpricecalculator.R;

public enum UnitType {
  WEIGHT(R.string.weight, "GRAM"),
  VOLUME(R.string.volume, "LITRE"),
  LENGTH(R.string.length, "METRE"),
  AREA(R.string.area, "SQUARE_METRE"),
  QUANTITY(R.string.quantity, "UNIT");

  private final int name;
  private final String base;

  UnitType(int name, String base) {
    this.name = name;
    this.base = base;
  }

  public int getName() {
    return name;
  }

  public Unit getBase() {
    return DefaultUnit.valueOf(base);
  }

  public static UnitType fromName(String name, Resources resources) {
    for (UnitType unitType : values()) {
      if (name.equals(resources.getString(unitType.getName()))) {
        return unitType;
      }
    }
    throw new IllegalArgumentException();
  }
}
