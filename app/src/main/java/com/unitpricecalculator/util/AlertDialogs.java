package com.unitpricecalculator.util;

import android.content.DialogInterface;
import android.os.Build.VERSION;
import androidx.appcompat.app.AlertDialog;

public final class AlertDialogs {

  private AlertDialogs() {}

  public static void materialize(AlertDialog alertDialog) {
    if (VERSION.SDK_INT < 28) {
      return;
    }

    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setAllCaps(false);
    alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setAllCaps(false);
    alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setAllCaps(false);
  }

}
