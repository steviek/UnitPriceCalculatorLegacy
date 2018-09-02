package com.unitpricecalculator.main;

import android.app.Activity;
import com.unitpricecalculator.comparisons.ComparisonFragment;
import com.unitpricecalculator.comparisons.UnitEntryViewModule;
import com.unitpricecalculator.inject.ActivityScoped;
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
            MenuFragment.Module.class,
            SavedFragment.Module.class,
            SettingsFragment.Module.class,
            UnitEntryViewModule.class})
    @ActivityScoped
    MainActivity contributeMainActivityInjector();

    @Module
    interface ActivityModule {

        @Binds
        Activity bindActivity(MainActivity activity);

        @Binds
        SavedFragment.Callback bindSavedFragmentCallback(MainActivity activity);

        @Binds
        MenuFragment.Callback bindMenuFragmentCallback(MainActivity activity);
    }
}
