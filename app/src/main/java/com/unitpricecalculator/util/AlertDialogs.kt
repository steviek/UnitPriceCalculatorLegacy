@file:JvmName("AlertDialogs")

package com.unitpricecalculator.util

import android.content.DialogInterface
import android.os.Build.VERSION
import androidx.appcompat.app.AlertDialog

fun AlertDialog.materialize() {
  if (VERSION.SDK_INT < 28) {
    return
  }

  getButton(DialogInterface.BUTTON_POSITIVE)?.isAllCaps = false
  getButton(DialogInterface.BUTTON_NEUTRAL)?.isAllCaps = false
  getButton(DialogInterface.BUTTON_NEGATIVE)?.isAllCaps = false
}