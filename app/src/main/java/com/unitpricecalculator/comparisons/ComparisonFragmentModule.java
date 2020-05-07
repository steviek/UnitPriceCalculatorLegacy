package com.unitpricecalculator.comparisons;

import com.unitpricecalculator.inject.FragmentScoped;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface ComparisonFragmentModule {
  @ContributesAndroidInjector
  @FragmentScoped
  ComparisonFragment contributeComparisonFragmentInjector();
}
