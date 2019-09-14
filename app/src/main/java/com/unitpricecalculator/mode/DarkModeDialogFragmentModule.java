package com.unitpricecalculator.mode;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DarkModeDialogFragmentModule {
  private DarkModeDialogFragmentModule() {}

  @ContributesAndroidInjector
  @FragmentScoped
  abstract DarkModeDialogFragment contributeDarkModeFragmentInjector();
}
