package com.unitpricecalculator.main;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.unitpricecalculator.comparisons.ComparisonFragment;
import com.unitpricecalculator.comparisons.UnitEntryViewModule;
import com.unitpricecalculator.inject.ActivityContext;
import com.unitpricecalculator.inject.ActivityScoped;
import com.unitpricecalculator.mode.DarkModeDialogFragmentModule;
import com.unitpricecalculator.saved.SavedFragment;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface MainActivityModule {

  @ContributesAndroidInjector(
      modules = {
          ActivityModule.class,
          ComparisonFragment.Module.class,
          DarkModeDialogFragmentModule.class,
          MenuFragment.Module.class,
          SavedFragment.Module.class,
          SettingsFragment.Module.class,
          UnitEntryViewModule.class})
  @ActivityScoped
  MainActivity contributeMainActivityInjector();

  @Module
  interface ActivityModule {

    @Binds
    @ActivityContext
    Context bindActivityContext(MainActivity activity);

    @Binds
    Activity bindActivity(MainActivity activity);

    @Binds
    AppCompatActivity bindAppCompatActivity(MainActivity activity);

    @Binds
    SavedFragment.Callback bindSavedFragmentCallback(MainActivity activity);

    @Binds
    MenuFragment.Callback bindMenuFragmentCallback(MainActivity activity);
  }
}
