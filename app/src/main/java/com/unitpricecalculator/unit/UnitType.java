package com.unitpricecalculator.unit;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.unitpricecalculator.R;

public enum UnitType {
  WEIGHT(R.string.weight, "GRAM"),
  VOLUME(R.string.volume, "LITRE"),
  LENGTH(R.string.length, "METRE"),
  AREA(R.string.area, "SQUARE_METRE"),
  QUANTITY(R.string.quantity, "UNIT");
  @StringRes private final int labelResId;
  private final String base;

  UnitType(@StringRes int labelResId, String base) {
    this.labelResId = labelResId;
    this.base = base;
  }

  @StringRes
  public int getLabelResId() {
    return labelResId;
  }

  @NonNull
  public String loadLabel(@NonNull Context context) {
    return context.getString(labelResId);
  }

  public DefaultUnit getBase() {
    return DefaultUnit.valueOf(base);
  }

  public static UnitType fromName(String name, Resources resources) {
    for (UnitType unitType : values()) {
      if (name.equals(resources.getString(unitType.getLabelResId()))) {
        return unitType;
      }
    }
    throw new IllegalArgumentException();
  }
}
