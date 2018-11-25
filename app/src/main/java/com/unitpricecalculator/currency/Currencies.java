package com.unitpricecalculator.currency;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.CurrencyChangedEvent;
import com.unitpricecalculator.unit.Units;
import dagger.Reusable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

@Reusable
public final class Currencies {

  private final Units units;
  private final Bus bus;
  private final Activity activity;

  @Inject
  Currencies(Units units, Bus bus, Activity activity) {
    this.units = units;
    this.bus = bus;
    this.activity = activity;
  }

  public void showChangeCurrencyDialog() {
    Currency currentCurrency = units.getCurrency();
    final List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());
    Collections.sort(currencies, (c1, c2) -> c1.getCurrencyCode().compareTo(c2.getCurrencyCode()));

    int currentCurrencyIndex = -1;
    String[] labels = new String[currencies.size()];
    for (int i = 0; i < labels.length; i++) {
      Currency currency = currencies.get(i);
      labels[i] = currency.getSymbol().equals(currency.getCurrencyCode())
          ? currency.getCurrencyCode()
          : currency.getCurrencyCode() + " (" + currency.getSymbol() + ")";
      if (currency.equals(currentCurrency)) {
        currentCurrencyIndex = i;
      }
    }

    new AlertDialog.Builder(activity)
        .setTitle(R.string.change_currency_symbol)
        .setSingleChoiceItems(
            labels,
            currentCurrencyIndex,
            (dialog, i) -> {
              Currency currency = currencies.get(i);
              units.setCurrency(currency);
              dialog.dismiss();
              bus.post(new CurrencyChangedEvent(currency));
            })
        .show();
  }

  public static Optional<Currency> parseCurrencySafely(String currencyCode) {
    Currency currency = null;

    try {
      currency = Currency.getInstance(currencyCode);
    } catch (Exception e) {
      // There was an exception getting the currency.
    }

    return Optional.fromNullable(currency);
  }

  public static Currency getSafeDefaultCurrency() {
    Currency currency = null;
    try {
      currency = Currency.getInstance(Locale.getDefault());
    } catch (Exception e) {
      // Locale did not have a currency.
    }
    return MoreObjects.firstNonNull(currency, Currency.getInstance("USD"));
  }

}
