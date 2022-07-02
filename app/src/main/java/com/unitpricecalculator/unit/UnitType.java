package com.unitpricecalculator.unit;

import android.content.Context;
import android.content.res.Resources;
import android.icu.text.MeasureFormat;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.unitpricecalculator.R;

public enum UnitType {
  WEIGHT(R.string.weight, MeasureFormat.FormatWidth.SHORT, "GRAM"),
  VOLUME(R.string.volume, MeasureFormat.FormatWidth.SHORT, "LITRE"),
  LENGTH(R.string.length, MeasureFormat.FormatWidth.SHORT, "METRE"),
  AREA(R.string.area, MeasureFormat.FormatWidth.SHORT, "SQUARE_METRE"),
  TIME(R.string.time, MeasureFormat.FormatWidth.WIDE, "DAY"),
  QUANTITY(R.string.quantity, MeasureFormat.FormatWidth.WIDE, "UNIT");
  @StringRes private final int labelResId;
  private final MeasureFormat.FormatWidth formatWidth;
  private final String base;

  UnitType(@StringRes int labelResId, MeasureFormat.FormatWidth formatWidth, String base) {
    this.labelResId = labelResId;
    this.formatWidth = formatWidth;
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

  @NonNull
  public MeasureFormat.FormatWidth getFormatWidth() {
    return formatWidth;
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
