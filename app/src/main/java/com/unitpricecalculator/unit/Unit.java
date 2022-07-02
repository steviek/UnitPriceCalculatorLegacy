package com.unitpricecalculator.unit;

import android.content.res.Resources;
import android.icu.util.MeasureUnit;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Unit extends Parcelable{
    String getSymbol(Resources resources);

    double getFactor();

    System getSystem();

    @NonNull
    UnitType getUnitType();

    @Nullable
    MeasureUnit getMeasureUnit();

    default int getDefaultQuantity() {
        return 1;
    }
}
