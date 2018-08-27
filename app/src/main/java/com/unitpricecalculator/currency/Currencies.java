package com.unitpricecalculator.currency;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.unitpricecalculator.R;
import com.unitpricecalculator.unit.Units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

public final class Currencies {

  public interface CurrencyDialogCallback {
    void onCurrencySelected(Currency currency);
  }

  public static void showChangeCurrencyDialog(Context context, @Nullable final CurrencyDialogCallback callback) {
    Currency currentCurrency = Units.getCurrency();
    final List<Currency> currencies = new ArrayList<>(Currency.getAvailableCurrencies());
    Collections.sort(currencies, new Comparator<Currency>() {
      @Override
      public int compare(Currency c1, Currency c2) {
        return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
      }
    });

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
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int i) {
                Currency currency = currencies.get(i);
                Units.setCurrency(currency);
                dialog.dismiss();
                if (callback != null) {
                  callback.onCurrencySelected(currency);
                }
              }
            })
        .show();
  }

}
