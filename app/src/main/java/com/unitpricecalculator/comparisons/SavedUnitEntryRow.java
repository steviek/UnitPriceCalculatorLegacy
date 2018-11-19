package com.unitpricecalculator.comparisons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.unitpricecalculator.unit.Unit;

public class SavedUnitEntryRow {

  private final String cost;
  private final String quantity;
  private final String size;
  private final Unit unit;
  private final String note;

  @JsonCreator
  public SavedUnitEntryRow(
      @JsonProperty("cost") String cost,
      @JsonProperty("quantity") String quantity,
      @JsonProperty("size") String size,
      @JsonProperty("unit") Unit unit,
      @JsonProperty("note") String note) {
    this.cost = Preconditions.checkNotNull(cost);
    this.quantity = Preconditions.checkNotNull(quantity);
    this.size = Preconditions.checkNotNull(size);
    this.unit = Preconditions.checkNotNull(unit);
    this.note = Strings.nullToEmpty(note);
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

  public String getNote() {
    return note;
  }

  @JsonIgnore
  boolean isEmpty() {
    if (!Strings.isNullOrEmpty(cost)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(quantity)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(size)) {
      return false;
    }

    if (!Strings.isNullOrEmpty(note)) {
      return false;
    }

    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SavedUnitEntryRow that = (SavedUnitEntryRow) o;

    if (cost != null ? !cost.equals(that.cost) : that.cost != null) {
      return false;
    }
    if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) {
      return false;
    }
    if (size != null ? !size.equals(that.size) : that.size != null) {
      return false;
    }
    if (!note.equals(this.note)) {
      return false;
    }
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

  @Override
  public String toString() {
    return "SavedUnitEntryRow{" +
        "cost='" + cost + '\'' +
        ", quantity='" + quantity + '\'' +
        ", size='" + size + '\'' +
        ", unit=" + unit +
        ", note='" + note + '\'' +
        '}';
  }
}
