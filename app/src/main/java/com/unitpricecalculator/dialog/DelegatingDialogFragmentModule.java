package com.unitpricecalculator.dialog;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class DelegatingDialogFragmentModule {

  private DelegatingDialogFragmentModule() {
  }

  @ContributesAndroidInjector
  @FragmentScoped
  abstract DelegatingDialogFragment contributeAndroidInjector();
}
