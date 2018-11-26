package com.unitpricecalculator.util;

import android.text.Editable;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.widget.EditText;
import com.google.common.base.Optional;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import java.text.DecimalFormatSymbols;

public final class Localization {

  public static void addLocalizedKeyListener(EditText editText) {
    char localeSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();
    if (localeSeparator == '.') {
      return;
    }

    String addLocaleMaybe = localeSeparator == ',' ? "" : "" + localeSeparator;

    KeyListener separatorKeyListener =
        DigitsKeyListener.getInstance("0123456789.," + addLocaleMaybe);
    editText.setKeyListener(separatorKeyListener);

    editText.addTextChangedListener(new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        boolean alreadyContainsSeparator = false;
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c == '.' || c == ',' || c == localeSeparator) {
            if (alreadyContainsSeparator) {
              s.replace(i, i + 1, "");
              break;
            } else {
              alreadyContainsSeparator = true;
            }
          }
        }
      }
    });
  }

  public static double parseDoubleOrThrow(String input) {
    try {
      return parseDoubleSafely(input).get();
    } catch (Exception e) {
      throw new NullPointerException();
    }
  }


  public static Optional<Double> parseDoubleSafely(String input) {
    char localeSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();
    if (localeSeparator == '.') {
      return maybeParseDouble(input);
    }

    return maybeParseDouble(input.replace(',', '.').replace(localeSeparator, '.'));
  }

  private static Optional<Double> maybeParseDouble(String input) {
    try {
      return Optional.of(Double.parseDouble(input));
    } catch (Exception e) {
      return Optional.absent();
    }
  }
}
