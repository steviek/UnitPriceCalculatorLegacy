package com.unitpricecalculator.util;

import android.text.Editable;
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

    KeyListener keyListener = LocalizedDigitsKeyListener.create();
    editText.setKeyListener(keyListener);

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
    int firstNonDigit = getIndexOfFirstNonDigit(input);
    if (firstNonDigit == -1) {
      return maybeParseDouble(input);
    }

    char separator = input.charAt(firstNonDigit);
    return maybeParseDouble(input.replace(separator, '.'));
  }

  private static int getIndexOfFirstNonDigit(String input) {
    int len = input.length();
    for (int i = 0; i < len; i++) {
      if (!Character.isDigit(input.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  private static Optional<Double> maybeParseDouble(String input) {
    try {
      return Optional.of(Double.parseDouble(input));
    } catch (Exception e) {
      return Optional.absent();
    }
  }
}
