package com.unitpricecalculator.application;

import android.app.Application;
import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.squareup.otto.Bus;
import com.unitpricecalculator.inject.ApplicationContext;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public interface SingletonModule {

    @Singleton
    @Provides
    static ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        objectMapper.enableDefaultTyping();
        return objectMapper;
    }

    @Singleton
    @Provides
    static Bus provideBus() {
        return new Bus();
    }

    @Binds
    @ApplicationContext
    Context bindContext(Application application);
}
