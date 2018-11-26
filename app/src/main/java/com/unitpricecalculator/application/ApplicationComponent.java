package com.unitpricecalculator.application;

import com.unitpricecalculator.json.JsonMapperModule;
import com.unitpricecalculator.main.MainActivityModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class, MainActivityModule.class, SingletonModule.class,
    JsonMapperModule.class,})
public interface ApplicationComponent {

  void inject(MyApplication application);
}
