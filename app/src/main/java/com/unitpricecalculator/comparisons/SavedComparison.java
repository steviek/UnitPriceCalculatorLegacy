package com.unitpricecalculator.comparisons;

import android.support.annotation.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitType;
import javax.annotation.Nullable;

public final class SavedComparison implements Comparable<SavedComparison> {

  private static long lastKeyGenerated;

  private final String key;
  private final String name;
  private final UnitType unitType;
  private final ImmutableList<SavedUnitEntryRow> savedUnitEntryRows;
  private final String finalQuantity;
  private final Unit finalUnit;
  @Nullable
  private final String currencyCode;

  @JsonCreator
  public SavedComparison(
      @JsonProperty("key") @Nullable String key,
      @JsonProperty("name") String name,
      @JsonProperty("unitType") UnitType unitType,
      @JsonProperty("entryRows") ImmutableList<SavedUnitEntryRow> savedUnitEntryRows,
      @JsonProperty("finalQuantity") String finalQuantity,
      @JsonProperty("finalUnit") Unit finalUnit,
      @JsonProperty("currencyCode") @Nullable String currencyCode) {

    if (key == null) {
      long newKey = System.currentTimeMillis();
      if (newKey == lastKeyGenerated) {
        newKey++;
      }
      lastKeyGenerated = newKey;
      key = String.valueOf(newKey);
    }
    this.key = key;
    this.name = name;
    this.unitType = unitType;
    this.savedUnitEntryRows = savedUnitEntryRows;
    this.finalQuantity = finalQuantity;
    this.finalUnit = finalUnit;
    this.currencyCode = currencyCode;
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

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public UnitType getUnitType() {
    return unitType;
  }

  @Nullable
  public String getCurrencyCode() {
    return currencyCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SavedComparison that = (SavedComparison) o;

    if (!key.equals(that.key)) {
      return false;
    }

    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (currencyCode != null ? !currencyCode.equals(that.currencyCode)
        : that.currencyCode != null) {
      return false;
    }
    if (unitType != that.unitType) {
      return false;
    }
    if (savedUnitEntryRows != null
        ? !savedUnitEntryRows.equals(that.savedUnitEntryRows)
        : that.savedUnitEntryRows != null) {
      return false;
    }
    if (finalQuantity != null
        ? !finalQuantity.equals(that.finalQuantity)
        : that.finalQuantity != null) {
      return false;
    }
    return !(finalUnit != null ? !finalUnit.equals(that.finalUnit) : that.finalUnit != null);

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (unitType != null ? unitType.hashCode() : 0);
    result = 31 * result + (savedUnitEntryRows != null ? savedUnitEntryRows.hashCode() : 0);
    result = 31 * result + (finalQuantity != null ? finalQuantity.hashCode() : 0);
    result = 31 * result + (finalUnit != null ? finalUnit.hashCode() : 0);
    result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SavedComparison{" +
        "name='" + name + '\'' +
        ", unitType=" + unitType +
        ", savedUnitEntryRows=" + savedUnitEntryRows +
        ", finalQuantity='" + finalQuantity + '\'' +
        ", finalUnit=" + finalUnit +
        ", currencyCode='" + currencyCode + '\'' +
        '}';
  }

  /**
   * Generates a copy of this comparison with the provided name.  The original object is unaffected.
   */
  public SavedComparison rename(String newName) {
    return new SavedComparison(key, newName, unitType, savedUnitEntryRows, finalQuantity, finalUnit,
        currencyCode);
  }

  @Override
  public int compareTo(@NonNull SavedComparison another) {
    return key.compareTo(another.key);
  }

  @JsonIgnore
  boolean isEmpty() {
    if (!Strings.isNullOrEmpty(name)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(finalQuantity)) {
      return false;
    }

    for (SavedUnitEntryRow entryRow : savedUnitEntryRows) {
      if (!entryRow.isEmpty()) {
        return false;
      }
    }

    return true;
  }
}
