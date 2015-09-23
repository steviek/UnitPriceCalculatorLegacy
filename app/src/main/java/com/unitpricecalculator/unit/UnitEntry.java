package com.unitpricecalculator.unit;

import com.google.common.base.Preconditions;

public final class UnitEntry {

  private final double cost;

  private final int quantity;

  private final double size;

  private final Unit unit;

  private UnitEntry(double cost, int quantity, double size, Unit unit) {
    Preconditions.checkArgument(cost >= 0);
    Preconditions.checkArgument(quantity > 0);
    Preconditions.checkArgument(size > 0);
    Preconditions.checkNotNull(unit);
    this.cost = cost;
    this.quantity = quantity;
    this.size = size;
    this.unit = unit;
  }

  public double getCost() {
    return cost;
  }

  public int getQuantity() {
    return quantity;
  }

  public double getSize() {
    return size;
  }

  public Unit getUnit() {
    return unit;
  }

  public double getPricePerUnit() {
    return cost / (quantity * size);
  }

  public double pricePer(double size, Unit unit) {
    Preconditions.checkArgument(unit.getUnitType() == this.unit.getUnitType());
    double costPerUnit = getPricePerUnit();
    double costPerOtherUnit = costPerUnit / (this.unit.getFactor() / unit.getFactor());
    return size * costPerOtherUnit;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private double cost;

    private int quantity;

    private double size;

    private Unit unit;

    private Builder() {
      this.quantity = 1;
    }

    public Builder setCost(double cost) {
      Preconditions.checkArgument(cost >= 0);
      this.cost = cost;
      return this;
    }

    public Builder setQuantity(int quantity) {
      Preconditions.checkArgument(quantity > 0);
      this.quantity = quantity;
      return this;
    }

    public Builder setUnit(Unit unit) {
      this.unit = Preconditions.checkNotNull(unit);
      return this;
    }

    public Builder setSize(double size) {
      Preconditions.checkArgument(size > 0);
      this.size = size;
      return this;
    }

    public UnitEntry build() {
      return new UnitEntry(cost, quantity, size, unit);
    }
  }
}
