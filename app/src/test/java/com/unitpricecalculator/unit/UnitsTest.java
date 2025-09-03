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
import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;
import dagger.hilt.android.testing.HiltTestApplication;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@HiltAndroidTest
@RunWith(RobolectricTestRunner.class)
@Config(sdk = P, application = HiltTestApplication.class)
public class UnitsTest {

  @Rule
  public final HiltAndroidRule hiltRule = new HiltAndroidRule(this);

  @Before
  public void setUp() {
    hiltRule.inject();
  }

  @Inject Systems systems;
  @Inject Units units;

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

  @Test
  public void formatter_integer_shouldFormatWithoutDecimals() {
    assertThat(units.getFormatter().format(12.00)).isEqualTo("$12");
  }

  @Test
  public void formatter_hasLeadingZeros_shouldTakeToFourDigits() {
    assertThat(units.getFormatter().format(0.00001234)).isEqualTo("$0.00001234");
  }

  @Test
  public void formatter_hasLeadingZeros_shouldNotGoBeyondFourExtraDigits() {
    assertThat(units.getFormatter().format(0.00123456)).isEqualTo("$0.001235");
  }

  @Test
  public void formatter_hasOneDecimal_shouldUseTwoDecimalPlaces() {
    assertThat(units.getFormatter().format(10.1)).isEqualTo("$10.10");
  }

  @Test
  public void formatter_hasLotsOfDecimals_isWhole_shouldOnlyUseTwoDecimalPlaces() {
    assertThat(units.getFormatter().format(123.45678)).isEqualTo("$123.46");
  }
}
