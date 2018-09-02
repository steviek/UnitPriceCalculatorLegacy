package com.unitpricecalculator.unit;

import android.content.res.Resources;
import android.os.Parcelable;

public interface Unit extends Parcelable{
    String getSymbol(Resources resources);

    double getFactor();

    System getSystem();

    UnitType getUnitType();

    default int getDefaultQuantity() {
        return 1;
    }
}
