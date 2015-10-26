package com.unitpricecalculator;

import android.app.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Prefs;

public final class MyApplication extends Application {

    private static MyApplication instance;

    private Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Prefs.initialize(this, new ObjectMapper());
        Logger.initialize("AppLogs", BuildConfig.DEBUG);
        bus = new Bus();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public Bus getBus() {
        return bus;
    }
}
