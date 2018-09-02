package com.unitpricecalculator.unit;

import com.unitpricecalculator.R;

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
}
