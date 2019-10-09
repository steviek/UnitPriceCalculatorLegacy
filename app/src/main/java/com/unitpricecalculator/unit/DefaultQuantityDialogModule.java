package com.unitpricecalculator.unit;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DefaultQuantityDialogModule {

  private DefaultQuantityDialogModule() {}

  @ContributesAndroidInjector
  @FragmentScoped
  abstract DefaultQuantityDialogFragment contributeAndroidInjector();
}
