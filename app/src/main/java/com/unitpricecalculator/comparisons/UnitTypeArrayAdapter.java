package com.unitpricecalculator.comparisons;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import com.unitpricecalculator.R;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

final class UnitTypeArrayAdapter extends ArrayAdapter<String> {

    @Inject
    UnitTypeArrayAdapter(Activity activity, Units units) {
        super(activity,
                R.layout.unit_type_spinner_dropdown_item,
                getValues(units, activity.getResources()));
    }

    private static List<String> getValues(Units units, Resources resources) {
        List<String> values = new ArrayList<>();
        for (UnitType unitType : UnitType.values()) {
            if (unitType == units.getCurrentUnitType()) {
                values.add(resources.getString(unitType.getName()));
            }
        }
        for (UnitType unitType : UnitType.values()) {
            if (unitType != units.getCurrentUnitType()) {
                values.add(resources.getString(unitType.getName()));
            }
        }
        return values;
    }
}
