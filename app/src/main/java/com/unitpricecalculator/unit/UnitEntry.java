package com.unitpricecalculator.unit;

import com.google.common.base.Preconditions;

public final class UnitEntry {

    private final double cost;

    private final String costString;

    private final int quantity;

    private final String quantityString;

    private final double size;

    private final String sizeString;

    private final Unit unit;

    private UnitEntry(double cost, String costString, int quantity, String quantityString, double size,
                      String sizeString, Unit unit) {
        Preconditions.checkArgument(cost >= 0);
        Preconditions.checkArgument(quantity > 0);
        Preconditions.checkArgument(size > 0);
        this.cost = cost;
        this.costString = Preconditions.checkNotNull(costString);
        this.quantity = quantity;
        this.quantityString = Preconditions.checkNotNull(quantityString);
        this.size = size;
        this.sizeString = Preconditions.checkNotNull(sizeString);
        this.unit = Preconditions.checkNotNull(unit);
    }

    public double getCost() {
        return cost;
    }

    public String getCostString() {
        return costString;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getQuantityString() {
        return quantityString;
    }

    public double getSize() {
        return size;
    }

    public String getSizeString() {
        return sizeString;
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

        private String costString;

        private int quantity;

        private String quantityString;

        private double size;

        private String sizeString;

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

        public Builder setCostString(String costString) {
            this.costString = Preconditions.checkNotNull(costString);
            return this;
        }

        public Builder setQuantityString(String quantityString) {
            this.quantityString = Preconditions.checkNotNull(quantityString);
            return this;
        }

        public Builder setSizeString(String sizeString) {
            this.sizeString = Preconditions.checkNotNull(sizeString);
            return this;
        }

        public UnitEntry build() {
            return new UnitEntry(cost, costString, quantity, quantityString, size, sizeString, unit);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
