package com.unitpricecalculator.unit;

import android.os.Parcelable;

public interface Unit extends Parcelable{
    String getSymbol();

    double getFactor();

    System getSystem();

    UnitType getUnitType();

    default int getDefaultQuantity() {
        return 1;
    }
}
