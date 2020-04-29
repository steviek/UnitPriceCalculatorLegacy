package com.unitpricecalculator.saved;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SavedFragmentModule {

  private SavedFragmentModule() {}

  @ContributesAndroidInjector()
  @FragmentScoped
  abstract SavedFragment contributeAndroidInjector();
}
