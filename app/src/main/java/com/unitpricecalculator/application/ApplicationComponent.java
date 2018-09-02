package com.unitpricecalculator.application;

import com.unitpricecalculator.main.MainActivityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class, MainActivityModule.class, SingletonModule.class} )
public interface ApplicationComponent {
    void inject(MyApplication application);
}
