package com.unitpricecalculator.unit;

import com.google.common.base.Preconditions;

import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;

import java.util.Arrays;
import java.util.Locale;

public enum System {
    METRIC(R.string.metric),
    IMPERIAL(R.string.imperial),
    IMPERIAL_UK(R.string.imperial_uk),
    IMPERIAL_US(R.string.imperial_us);

    private final int name;

    System(int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }

    public boolean is(System other) {
        if (this == METRIC) {
            return other == METRIC;
        } else if (this == IMPERIAL) {
            return this != METRIC;
        } else if (this == IMPERIAL_UK) {
            return other == IMPERIAL_UK || other == IMPERIAL;
        } else {
            return other == IMPERIAL_US || other == IMPERIAL;
        }
    }

    private static String getDefaultOrder() {
        String country = Locale.getDefault().getCountry();
        if (country != null && (country.equalsIgnoreCase("US") || country.equalsIgnoreCase("USA"))) {
            return IMPERIAL_US + "," + METRIC + "," + IMPERIAL_UK;
        } else {
            return METRIC + "," + IMPERIAL_UK + "," + IMPERIAL_US;
        }
    }

    public static System[] getPreferredOrder() {
        String order = Prefs.getString(Keys.SYSTEM_ORDER, getDefaultOrder());
        String[] parts = order.split(",");
        System[] result = new System[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = System.valueOf(parts[i]);
        }
        Logger.d("Get preferred order: %s", Arrays.toString(result));
        return result;
    }

    public static void setPreferredOrder(System[] order) {
        Preconditions.checkArgument(order.length == 3);
        Prefs.putString(Keys.SYSTEM_ORDER, order[0] + "," + order[1] + "," + order[2]);
        Logger.d("Set preferred order: %s", Arrays.toString(order));
        MyApplication.getInstance().getBus().post(new SystemChangedEvent(order));
    }
}
