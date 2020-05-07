package com.unitpricecalculator.main;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.unitpricecalculator.comparisons.ComparisonFragmentModule;
import com.unitpricecalculator.comparisons.UnitEntryViewModule;
import com.unitpricecalculator.dialog.DelegatingDialogFragmentModule;
import com.unitpricecalculator.inject.ActivityContext;
import com.unitpricecalculator.inject.ActivityScoped;
import com.unitpricecalculator.mode.DarkModeDialogFragmentModule;
import com.unitpricecalculator.saved.SavedFragment;
import com.unitpricecalculator.saved.SavedFragmentModule;
import com.unitpricecalculator.settings.SettingsModule;
import com.unitpricecalculator.unit.DefaultQuantityDialogModule;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface MainActivityModule {

  @ContributesAndroidInjector(
      modules = {
          ActivityModule.class,
          ComparisonFragmentModule.class,
          DarkModeDialogFragmentModule.class,
          DelegatingDialogFragmentModule.class,
          DefaultQuantityDialogModule.class,
          MenuFragment.Module.class,
          SavedFragmentModule.class,
          SettingsModule.class,
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
