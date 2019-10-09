package com.unitpricecalculator.settings;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SettingsModule {

  private SettingsModule() {
  }

  @ContributesAndroidInjector
  @FragmentScoped
  abstract SettingsFragment contributeAndroidInjector();
}
