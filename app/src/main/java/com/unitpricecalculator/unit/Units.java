package com.unitpricecalculator.unit;

import androidx.annotation.Nullable;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.squareup.otto.Bus;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.Reusable;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/**
 * Collection of utility functions for {@link Unit}.
 */
@Reusable
public final class Units {

  private static final String KEY_CURRENCY = "currency";

  private final Prefs prefs;
  private final Bus bus;

  @Nullable
  private Currency currency;
  @Nullable
  private Function<Double, String> costFormatter;

  @Inject
  Units(Prefs prefs, Bus bus) {
    this.prefs = prefs;
    this.bus = bus;
  }

  private static Map<UnitType, ImmutableList<DefaultUnit>> unitMap = new HashMap<>();

  public ImmutableList<DefaultUnit> getUnitsForType(UnitType unitType) {
    if (unitMap.get(unitType) == null) {
      ImmutableList.Builder<DefaultUnit> list = ImmutableList.builder();
      for (DefaultUnit unit : DefaultUnit.values()) {
        if (unit.getUnitType() == unitType) {
          list.add(unit);
        }
      }
      unitMap.put(unitType, list.build());
    }
    return unitMap.get(unitType);
  }

  public UnitType getCurrentUnitType() {
    return UnitType.valueOf(prefs.getString(Keys.UNIT_TYPE, UnitType.WEIGHT.name()));
  }

  public void setCurrentUnitType(UnitType unitType) {
    prefs.putString(Keys.UNIT_TYPE, unitType.name());
    bus.post(new UnitTypeChangedEvent(unitType));
  }

  public Currency getCurrency() {
    if (currency != null) {
      return currency;
    }

    String savedCurrency = prefs.getString(KEY_CURRENCY);
    if (savedCurrency != null) {
      currency =
          Currencies.parseCurrencySafely(savedCurrency).or(Currencies.getSafeDefaultCurrency());
      costFormatter = null;
      return currency;
    }

    currency = Currencies.getSafeDefaultCurrency();
    costFormatter = null;
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
    this.costFormatter = null;
    prefs.putString(KEY_CURRENCY, currency.getCurrencyCode());
  }

  public Function<Double, String> getFormatter() {
    if (costFormatter == null) {
      costFormatter = createFormatter();
    }
    return costFormatter;
  }

  private Function<Double, String> createFormatter() {
    NumberFormat lotsOfDigits = NumberFormat.getCurrencyInstance();
    lotsOfDigits.setCurrency(currency);
    lotsOfDigits.setMinimumFractionDigits(2);
    lotsOfDigits.setMaximumFractionDigits(8);

    NumberFormat defaultForCurrency = NumberFormat.getCurrencyInstance();
    defaultForCurrency.setCurrency(currency);

    return input -> {
      String formattedPricePer = lotsOfDigits.format(input);

      String[] parts = formattedPricePer.split("[.,]");
      if (parts.length != 2) {
        return formattedPricePer;
      }

      // Attempt to account for rounding errors and excessive digits by checking for repeating
      // digits after the decimal.
      String decimalPortion = parts[1];
      int longestRunLength = 1;
      int currentRunLength = 1;
      char lastDigit = decimalPortion.charAt(0);
      for (int i = 1; i < decimalPortion.length(); i++) {
        if (decimalPortion.charAt(i) == lastDigit) {
          currentRunLength += 1;
          if (currentRunLength > longestRunLength) {
            longestRunLength = currentRunLength;
          }
        } else {
          currentRunLength = 1;
          lastDigit = decimalPortion.charAt(i);
        }
      }

      if (longestRunLength >= 4) {
        formattedPricePer = defaultForCurrency.format(input);
      }

      return formattedPricePer;
    };
  }
}
