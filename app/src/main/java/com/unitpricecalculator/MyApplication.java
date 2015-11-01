package com.unitpricecalculator;

import android.app.Application;

import com.crittercism.app.Crittercism;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.squareup.otto.Bus;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Prefs;

public final class MyApplication extends Application {

    private static MyApplication instance;

    private Bus bus;

    private ObjectMapper objectMapper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        objectMapper.enableDefaultTyping();
        Prefs.initialize(this, objectMapper);
        Logger.initialize("AppLogs", BuildConfig.DEBUG);
        bus = new Bus();
        Crittercism.initialize(this, "56367aca8d4d8c0a00d08069");
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public Bus getBus() {
        return bus;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
