package com.unitpricecalculator.main;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;

import java.util.ArrayList;
import java.util.List;

final class UnitTypeArrayAdapter extends ArrayAdapter<String> {

    UnitTypeArrayAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item, getValues(context.getResources()));
    }

    private static List<String> getValues(Resources resources) {
        List<String> values = new ArrayList<>();
        for (UnitType unitType : UnitType.values()) {
            if (unitType == Units.getCurrentUnitType()) {
                values.add(resources.getString(unitType.getName()));
            }
        }
        for (UnitType unitType : UnitType.values()) {
            if (unitType != Units.getCurrentUnitType()) {
                values.add(resources.getString(unitType.getName()));
            }
        }
        return values;
    }
}
