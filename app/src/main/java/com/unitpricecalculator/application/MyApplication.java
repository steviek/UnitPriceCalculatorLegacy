package com.unitpricecalculator.application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.google.android.material.color.DynamicColors;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.locale.AppLocaleManager;
import com.unitpricecalculator.mode.DarkModeManager;
import com.unitpricecalculator.mode.DarkModeStateChangedEvent;
import dagger.hilt.android.HiltAndroidApp;

import javax.inject.Inject;

@HiltAndroidApp
public final class MyApplication extends MultiDexApplication {

  @Inject DarkModeManager darkModeManager;
  @Inject Bus bus;
  @Inject AppLocaleManager appLocaleManager;

  @Override
  public void onCreate() {
    super.onCreate();
    bus.register(this);
    AppCompatDelegate.setDefaultNightMode(darkModeManager.getCurrentDarkModeState().getNightMode());
    DynamicColors.applyToActivitiesIfAvailable(this);
  }

  @Subscribe
  public void onDarkModeStateChanged(DarkModeStateChangedEvent event) {
    AppCompatDelegate.setDefaultNightMode(event.getNewState().getNightMode());
  }
}
