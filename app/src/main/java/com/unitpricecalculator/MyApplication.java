package com.unitpricecalculator;

import android.app.Application;

import com.google.common.collect.ImmutableMap;

import com.squareup.otto.Bus;
import com.unitpricecalculator.unit.CustomUnit;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Prefs;
import com.unitpricecalculator.util.prefs.StringDeserializer;
import com.unitpricecalculator.util.prefs.StringSerializer;

public final class MyApplication extends Application {

    private static MyApplication instance;

    private static ImmutableMap<Class<?>, StringSerializer<?>> serializers =
            ImmutableMap.<Class<?>, StringSerializer<?>>builder()
                    .put(CustomUnit.class, CustomUnit.STRING_SERIALIZER)
                    .build();

    private static ImmutableMap<Class<?>, StringDeserializer<?>> deserializers =
            ImmutableMap.<Class<?>, StringDeserializer<?>>builder()
                    .put(CustomUnit.class, CustomUnit.STRING_DESERIALIZER)
                    .build();

    private Bus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Prefs.initialize(this, serializers, deserializers);
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
