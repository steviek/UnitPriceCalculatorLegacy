package com.unitpricecalculator.util;

import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import com.unitpricecalculator.util.abstracts.DelegatingKeyListener;
import java.text.DecimalFormatSymbols;

final class LocalizedDigitsKeyListener extends DelegatingKeyListener {

  static KeyListener create() {
    char localeSeparator = DecimalFormatSymbols.getInstance().getMonetaryDecimalSeparator();
    String addLocaleMaybe = localeSeparator == ',' ? "" : "" + localeSeparator;
    KeyListener separatorKeyListener =
        DigitsKeyListener.getInstance("0123456789,." + addLocaleMaybe);
    return new LocalizedDigitsKeyListener(separatorKeyListener);
  }

  private LocalizedDigitsKeyListener(KeyListener delegate) {
    super(delegate);
  }

  @Override
  public int getInputType() {
    // DigitsKeyListener does not attempt to parse the decimal flag value when providing a list
    // of accepted chars. Therefore, explicitly return the correct input type so that keyboards
    // not acting based on accepted chars (ahem...Samsung) at least show the proper keyboard with
    // the decimal instead of the number one.
    return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
  }
}
