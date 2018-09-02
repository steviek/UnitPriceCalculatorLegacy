package com.unitpricecalculator.unit;

import static com.unitpricecalculator.unit.System.IMPERIAL_UK;
import static com.unitpricecalculator.unit.System.IMPERIAL_US;
import static com.unitpricecalculator.unit.System.METRIC;

import com.google.common.base.Preconditions;
import com.squareup.otto.Bus;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.Reusable;
import java.util.Arrays;
import java.util.Locale;
import javax.inject.Inject;

@Reusable
public final class Systems {

    private final Prefs prefs;
    private final Bus bus;

    @Inject
    Systems(Prefs prefs, Bus bus) {
        this.prefs = prefs;
        this.bus = bus;
    }

    private String getDefaultOrder() {
        String country = Locale.getDefault().getCountry();
        if (country != null && (country.equalsIgnoreCase("US") || country.equalsIgnoreCase("USA"))) {
            return IMPERIAL_US + "," + METRIC + "," + IMPERIAL_UK;
        } else {
            return METRIC + "," + IMPERIAL_UK + "," + IMPERIAL_US;
        }
    }

    public System[] getPreferredOrder() {
        String order = prefs.getString(Keys.SYSTEM_ORDER, getDefaultOrder());
        String[] parts = order.split(",");
        System[] result = new System[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = System.valueOf(parts[i]);
        }
        Logger.d("Get preferred order: %s", Arrays.toString(result));
        return result;
    }

    public void setPreferredOrder(System[] order) {
        Preconditions.checkArgument(order.length == 3);
        prefs.putString(Keys.SYSTEM_ORDER, order[0] + "," + order[1] + "," + order[2]);
        Logger.d("Set preferred order: %s", Arrays.toString(order));
        bus.post(new SystemChangedEvent(order));
    }
}
