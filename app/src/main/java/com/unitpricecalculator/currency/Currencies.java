package com.unitpricecalculator.currency;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.unitpricecalculator.R;
import com.unitpricecalculator.unit.Units;

import dagger.Reusable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import javax.inject.Inject;

@Reusable
public final class Currencies {

  private final Units units;

  @Inject
  Currencies(Units units) {
    this.units = units;
  }

  public interface CurrencyDialogCallback {
    void onCurrencySelected(Currency currency);
  }

  public void showChangeCurrencyDialog(Context context, @Nullable final CurrencyDialogCallback callback) {
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

    new AlertDialog.Builder(context)
        .setTitle(R.string.change_currency_symbol)
        .setSingleChoiceItems(
            labels,
            currentCurrencyIndex,
                (dialog, i) -> {
                  Currency currency = currencies.get(i);
                  units.setCurrency(currency);
                  dialog.dismiss();
                  if (callback != null) {
                    callback.onCurrencySelected(currency);
                  }
                })
        .show();
  }

}
