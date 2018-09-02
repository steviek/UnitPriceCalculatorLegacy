package com.unitpricecalculator.comparisons;

import android.view.View;
import com.unitpricecalculator.comparisons.UnitEntryViewModule.UnitEntryViewComponent;
import com.unitpricecalculator.inject.ViewKey;
import com.unitpricecalculator.unit.Units;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;
import javax.inject.Provider;

@Module(subcomponents = UnitEntryViewComponent.class)
public interface UnitEntryViewModule {

  @Provides
  @IntoMap
  @ViewKey(UnitEntryView.class)
  static AndroidInjector.Factory<? extends View> provideAndroidInjector(
      Provider<Units> units, Provider<UnitArrayAdapterFactory> unitArrayAdapterFactory) {
    // TODO: figure out how to generate this with dagger
    return instance -> instance1 -> {
      UnitEntryView unitEntryView = (UnitEntryView) instance1;
      unitEntryView.units = units.get();
      unitEntryView.unitArrayAdapterFactory = unitArrayAdapterFactory.get();
    };
  }

  @Subcomponent
  interface UnitEntryViewComponent {

    void inject(UnitEntryView unitEntryView);

    @Subcomponent.Builder
    interface Builder {
      UnitEntryViewComponent build();
    }
  }
}

