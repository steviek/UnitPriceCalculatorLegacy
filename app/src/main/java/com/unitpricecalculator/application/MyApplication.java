package com.unitpricecalculator.application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.locale.AppLocaleManager;
import com.unitpricecalculator.mode.DarkModeManager;
import com.unitpricecalculator.mode.DarkModeStateChangedEvent;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import javax.inject.Inject;

public final class MyApplication extends MultiDexApplication implements HasAndroidInjector {

  @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;
  @Inject DarkModeManager darkModeManager;
  @Inject Bus bus;
  @Inject AppLocaleManager appLocaleManager;

  @Override
  public void onCreate() {
    super.onCreate();
    DaggerApplicationComponent
        .builder()
        .applicationModule(new ApplicationModule(this))
        .build()
        .inject(this);
    bus.register(this);
    AppCompatDelegate.setDefaultNightMode(darkModeManager.getCurrentDarkModeState().getNightMode());
  }

  @Override
  public AndroidInjector<Object> androidInjector() {
    return dispatchingAndroidInjector;
  }

  @Subscribe
  public void onDarkModeStateChanged(DarkModeStateChangedEvent event) {
    AppCompatDelegate.setDefaultNightMode(event.getNewState().getNightMode());
  }
}
