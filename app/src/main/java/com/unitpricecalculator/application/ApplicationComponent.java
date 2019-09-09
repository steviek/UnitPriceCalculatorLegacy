package com.unitpricecalculator.application;

import com.unitpricecalculator.json.JsonMapperModule;
import com.unitpricecalculator.main.MainActivityModule;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
        AndroidInjectionModule.class,
        ApplicationModule.class,
        MainActivityModule.class,
        SingletonModule.class,
        JsonMapperModule.class,
    })
public interface ApplicationComponent {

  void inject(MyApplication application);
}
