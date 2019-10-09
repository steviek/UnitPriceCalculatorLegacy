package com.unitpricecalculator.unit;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static com.unitpricecalculator.unit.DefaultUnit.GRAM;
import static com.unitpricecalculator.unit.DefaultUnit.LITRE;
import static com.unitpricecalculator.unit.DefaultUnit.OUNCE;
import static com.unitpricecalculator.unit.DefaultUnit.POUND;
import static com.unitpricecalculator.unit.System.IMPERIAL_UK;
import static com.unitpricecalculator.unit.System.IMPERIAL_US;
import static com.unitpricecalculator.unit.System.METRIC;
import static com.unitpricecalculator.unit.UnitType.VOLUME;
import static com.unitpricecalculator.unit.UnitType.WEIGHT;

import com.google.common.collect.ImmutableSet;
import com.unitpricecalculator.application.SingletonModule;
import com.unitpricecalculator.json.SerializersModule;
import dagger.Component;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = P)
public class UnitsTest {

  @Inject Systems systems;
  @Inject Units units;

  @Before
  public void setUp() {
    DaggerUnitsTest_TestComponent.create().inject(this);
  }

  @Test
  public void defaultQuantity_notSet_shouldUseFallback() {
    assertThat(units.getDefaultQuantity(WEIGHT)).isEqualTo(new Quantity(1, OUNCE));
  }

  @Test
  public void defaultQuantity_notSet_shouldUseDefaultForPreferredSystem() {
    systems.setPreferredOrder(new System[]{METRIC, IMPERIAL_UK, IMPERIAL_US});

    assertThat(units.getDefaultQuantity(WEIGHT)).isEqualTo(new Quantity(100, GRAM));
  }

  @Test
  public void defaultQuantity_notSet_shouldUseDefaultFromIncludedSystemsOnly() {
    systems.setIncludedSystems(ImmutableSet.of(METRIC));

    assertThat(units.getDefaultQuantity(WEIGHT)).isEqualTo(new Quantity(100, GRAM));
  }

  @Test
  public void defaultQuantity_set_shouldUseSetValue() {
    units.setDefaultQuantity(WEIGHT, new Quantity(50, POUND));

    assertThat(units.getDefaultQuantity(WEIGHT)).isEqualTo(new Quantity(50, POUND));
  }

  @Test
  public void defaultQuantity_set_shouldUseModifyOtherValues() {
    units.setDefaultQuantity(WEIGHT, new Quantity(50, POUND));
    units.setDefaultQuantity(VOLUME, new Quantity(2.5, LITRE));

    assertThat(units.getDefaultQuantity(WEIGHT)).isEqualTo(new Quantity(50, POUND));
    assertThat(units.getDefaultQuantity(VOLUME)).isEqualTo(new Quantity(2.5, LITRE));
  }

  @Test
  public void defaultQuantity_shouldUseCurrentUnitType() {
    units.setDefaultQuantity(VOLUME, new Quantity(2.5, LITRE));
    units.setCurrentUnitType(VOLUME);

    assertThat(units.getDefaultQuantity()).isEqualTo(new Quantity(2.5, LITRE));
  }

  @Singleton
  @Component(modules = {
      SerializersModule.class,
      SingletonModule.class,
      TestApplicationModule.class
  })
  interface TestComponent {

    void inject(UnitsTest test);
  }
}
