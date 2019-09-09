package com.unitpricecalculator.comparisons;

import android.app.Activity;
import com.squareup.otto.Bus;
import com.unitpricecalculator.comparisons.UnitEntryViewModule.UnitEntryViewComponent;
import com.unitpricecalculator.unit.Units;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import javax.inject.Provider;

@Module(subcomponents = UnitEntryViewComponent.class)
public interface UnitEntryViewModule {

  @Provides
  @IntoMap
  @ClassKey(UnitEntryView.class)
  static AndroidInjector.Factory<?> provideAndroidInjector(
      Provider<Activity> activity,
      Provider<Units> units,
      Provider<UnitArrayAdapterFactory> unitArrayAdapterFactory,
      Provider<Bus> bus) {
    // TODO: figure out how to generate this with dagger
    return instance -> instance1 -> {
      UnitEntryView unitEntryView = (UnitEntryView) instance1;
      unitEntryView.activity = activity.get();
      unitEntryView.units = units.get();
      unitEntryView.unitArrayAdapterFactory = unitArrayAdapterFactory.get();
      unitEntryView.bus = bus.get();
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

